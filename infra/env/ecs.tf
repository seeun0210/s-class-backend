# ──────────────────────────────────────
# ECS Resources (prod only)
# ──────────────────────────────────────

# ──────────────────────────────────────
# Security Groups
# ──────────────────────────────────────
resource "aws_security_group" "alb" {
  count       = local.is_prod ? 1 : 0
  name_prefix = "${local.name_prefix}-alb-"
  description = "Application Load Balancer"
  vpc_id      = local.shared.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-alb"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group" "ecs_tasks" {
  count       = local.is_prod ? 1 : 0
  name_prefix = "${local.name_prefix}-ecs-tasks-"
  description = "ECS Fargate Tasks"
  vpc_id      = local.shared.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb[0].id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-ecs-tasks"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ECS Tasks → RDS
resource "aws_security_group_rule" "ecs_to_rds" {
  count = local.is_prod ? 1 : 0

  type                     = "egress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_tasks[0].id
  source_security_group_id = aws_security_group.rds.id
  description              = "ECS Tasks to RDS MySQL"
}

resource "aws_security_group_rule" "rds_from_ecs" {
  count = local.is_prod ? 1 : 0

  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = aws_security_group.ecs_tasks[0].id
  description              = "RDS MySQL from ECS Tasks"
}

# ──────────────────────────────────────
# ACM Certificate
# ──────────────────────────────────────
resource "aws_acm_certificate" "main" {
  count = local.is_prod ? 1 : 0

  domain_name               = "*.${local.domain_suffix}"
  subject_alternative_names = [local.domain_suffix]
  validation_method         = "DNS"

  tags = {
    Name = "${local.name_prefix}-cert"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "cert_validation" {
  for_each = local.is_prod ? {
    for dvo in aws_acm_certificate.main[0].domain_validation_options :
    dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  } : {}

  zone_id         = local.shared.route53_zone_id
  name            = each.value.name
  type            = each.value.type
  ttl             = 60
  records         = [each.value.record]
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "main" {
  count = local.is_prod ? 1 : 0

  certificate_arn         = aws_acm_certificate.main[0].arn
  validation_record_fqdns = [for r in aws_route53_record.cert_validation : r.fqdn]
}

# ──────────────────────────────────────
# Application Load Balancer
# ──────────────────────────────────────
resource "aws_lb" "main" {
  count = local.is_prod ? 1 : 0

  name               = "${local.name_prefix}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb[0].id]
  subnets            = local.shared.public_subnets

  enable_deletion_protection = true

  tags = {
    Name = "${local.name_prefix}-alb"
  }
}

resource "aws_lb_listener" "http" {
  count = local.is_prod ? 1 : 0

  load_balancer_arn = aws_lb.main[0].arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https" {
  count = local.is_prod ? 1 : 0

  load_balancer_arn = aws_lb.main[0].arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate_validation.main[0].certificate_arn

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

# ──────────────────────────────────────
# Target Groups & Listener Rules
# ──────────────────────────────────────
resource "aws_lb_target_group" "services" {
  for_each = local.is_prod ? var.services : {}

  name        = "${local.name_prefix}-${each.key}"
  port        = tonumber(each.value.port)
  protocol    = "HTTP"
  vpc_id      = local.shared.vpc_id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_listener_rule" "services" {
  for_each = local.is_prod ? var.services : {}

  listener_arn = aws_lb_listener.https[0].arn
  priority     = index(keys(var.services), each.key) + 100

  condition {
    host_header {
      values = ["${each.key}.${local.domain_suffix}"]
    }
  }

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.services[each.key].arn
  }
}

# ──────────────────────────────────────
# CloudWatch Log Groups
# ──────────────────────────────────────
resource "aws_cloudwatch_log_group" "ecs" {
  for_each = local.is_prod ? var.services : {}

  name              = "/ecs/${local.name_prefix}/${each.key}"
  retention_in_days = 30

  tags = {
    Name = "${local.name_prefix}-${each.key}-logs"
  }
}

# ──────────────────────────────────────
# ECS Cluster
# ──────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  count = local.is_prod ? 1 : 0
  name  = local.name_prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = local.name_prefix
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  count        = local.is_prod ? 1 : 0
  cluster_name = aws_ecs_cluster.main[0].name

  capacity_providers = ["FARGATE"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }
}

# ──────────────────────────────────────
# ECS Task Definitions
# ──────────────────────────────────────
locals {
  ecs_env_vars = {
    for key, svc in var.services : key => [
      { name = "AWS_REGION", value = var.aws_region },
      { name = "SERVER_PORT", value = svc.port },
      { name = "DATASOURCE_URL", value = "jdbc:mysql://${local.rds_endpoint}/${var.db_name}" },
      { name = "SPRING_JPA_HIBERNATE_DDL_AUTO", value = local.is_prod ? "validate" : "update" },
      { name = "S3_BUCKET", value = aws_s3_bucket.main.id },
      { name = "S3_REGION", value = var.aws_region },
      { name = "CORS_ALLOW_ORIGINS", value = "${var.cors_allow_origins},https://report.aura.co.kr" },
      { name = "SMTP_ENABLED", value = "true" },
      { name = "SMTP_HOST", value = var.smtp_host },
      { name = "SMTP_PORT", value = var.smtp_port },
      { name = "CLOUDWATCH_METRICS_ENABLED", value = local.is_prod ? "true" : "false" },
      { name = "CLOUDWATCH_NAMESPACE", value = "SClass/${title(key)}" },
      { name = "ALIMTALK_ENABLED", value = "true" },
      { name = "ALIMTALK_PLUS_FRIEND_ID", value = "@학생부종합전형" },
      { name = "ALIMTALK_APP_BASE_URL", value = var.alimtalk_app_base_url },
      { name = "FRONTEND_URL", value = key == "supporters-api" ? var.frontend_url : "" },
      { name = "JWT_ACCESS_EXP", value = var.jwt_access_exp },
      { name = "JWT_REFRESH_EXP", value = var.jwt_refresh_exp },
      { name = "REPORT_SERVICE_ENABLED", value = contains(["backoffice-api", "supporters-api"], key) ? "true" : "false" },
      { name = "REPORT_SERVICE_BASE_URL", value = contains(["backoffice-api", "supporters-api"], key) ? var.report_service_base_url : "" },
      { name = "REPORT_SERVICE_CALLBACK_BASE_URL", value = key == "supporters-api" ? var.report_service_callback_base_url : "" },
    ]
  }

  ecs_secrets = {
    for key, svc in var.services : key => concat([
      { name = "DATASOURCE_USERNAME", valueFrom = aws_ssm_parameter.db_username.arn },
      { name = "DATASOURCE_PASSWORD", valueFrom = aws_ssm_parameter.db_password.arn },
      { name = "JWT_SECRET_KEY", valueFrom = aws_ssm_parameter.jwt_secret_key.arn },
      { name = "TOKEN_ENCRYPTION_KEY", valueFrom = aws_ssm_parameter.token_encryption_key.arn },
      { name = "GOOGLE_CLIENT_ID", valueFrom = aws_ssm_parameter.google_client_id.arn },
      { name = "KAKAO_CLIENT_ID", valueFrom = aws_ssm_parameter.kakao_client_id.arn },
      { name = "KAKAO_APP_ID", valueFrom = aws_ssm_parameter.kakao_app_id.arn },
      { name = "SMTP_USERNAME", valueFrom = aws_ssm_parameter.smtp_username.arn },
      { name = "SMTP_PASSWORD", valueFrom = aws_ssm_parameter.smtp_password.arn },
      { name = "ALIMTALK_ACCESS_KEY", valueFrom = aws_ssm_parameter.alimtalk_access_key.arn },
      { name = "ALIMTALK_SERVICE_ID", valueFrom = aws_ssm_parameter.alimtalk_service_id.arn },
      { name = "ALIMTALK_SECRET_KEY", valueFrom = aws_ssm_parameter.alimtalk_secret_key.arn },
      { name = "REDIS_HOST", valueFrom = aws_ssm_parameter.redis_host.arn },
      { name = "REDIS_PORT", valueFrom = aws_ssm_parameter.redis_port.arn },
      { name = "NICE_PAY_CLIENT_KEY", valueFrom = aws_ssm_parameter.nicepay_client_key.arn },
      { name = "NICE_PAY_SECRET_KEY", valueFrom = aws_ssm_parameter.nicepay_secret_key.arn },
      { name = "REPORT_SERVICE_CALLBACK_SECRET", valueFrom = aws_ssm_parameter.report_service_callback_secret.arn },
    ], local.is_prod ? [] : [
      { name = "REDIS_PASSWORD", valueFrom = aws_ssm_parameter.redis_password.arn },
    ])
  }
}

resource "aws_ecs_task_definition" "services" {
  for_each = local.is_prod ? var.services : {}

  family                   = "${local.name_prefix}-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_cpu
  memory                   = var.ecs_memory
  execution_role_arn       = aws_iam_role.ecs_execution[0].arn
  task_role_arn            = aws_iam_role.ecs_task[0].arn

  container_definitions = jsonencode([{
    name      = each.key
    image     = "${aws_ecr_repository.services[each.key].repository_url}:latest"
    essential = true

    portMappings = [{
      containerPort = tonumber(each.value.port)
      protocol      = "tcp"
    }]

    environment = local.ecs_env_vars[each.key]
    secrets     = local.ecs_secrets[each.key]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.ecs[each.key].name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget -q --spider http://localhost:${each.value.port}/swagger-ui.html || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 90
    }
  }])

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }
}

# ──────────────────────────────────────
# ECS Services
# ──────────────────────────────────────
resource "aws_ecs_service" "services" {
  for_each = local.is_prod ? var.services : {}

  name            = "${local.name_prefix}-${each.key}"
  cluster         = aws_ecs_cluster.main[0].id
  task_definition = aws_ecs_task_definition.services[each.key].arn
  desired_count   = each.value.min_size

  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
    base              = 1
  }

  network_configuration {
    subnets          = local.shared.private_subnets
    security_groups  = [aws_security_group.ecs_tasks[0].id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.services[each.key].arn
    container_name   = each.key
    container_port   = tonumber(each.value.port)
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  lifecycle {
    ignore_changes = [desired_count, task_definition]
  }

  depends_on = [aws_lb_listener.https]

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }
}

# ──────────────────────────────────────
# Auto Scaling
# ──────────────────────────────────────
resource "aws_appautoscaling_target" "services" {
  for_each = local.is_prod ? var.services : {}

  max_capacity       = each.value.max_size
  min_capacity       = each.value.min_size
  resource_id        = "service/${aws_ecs_cluster.main[0].name}/${aws_ecs_service.services[each.key].name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "services_cpu" {
  for_each = local.is_prod ? var.services : {}

  name               = "${local.name_prefix}-${each.key}-cpu"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.services[each.key].resource_id
  scalable_dimension = aws_appautoscaling_target.services[each.key].scalable_dimension
  service_namespace  = aws_appautoscaling_target.services[each.key].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value       = 70.0
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}
