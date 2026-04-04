# ──────────────────────────────────────
# ECS Cluster
# ──────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  name = local.name_prefix

  tags = {
    Name = "${local.name_prefix}-cluster"
  }
}

# ──────────────────────────────────────
# CloudWatch Log Groups
# ──────────────────────────────────────
resource "aws_cloudwatch_log_group" "services" {
  for_each = var.services

  name              = "/ecs/${local.name_prefix}-${each.key}"
  retention_in_days = 30

  tags = {
    Name = "${local.name_prefix}-${each.key}-logs"
  }
}

# ──────────────────────────────────────
# ECS Express Gateway Services
# ──────────────────────────────────────
locals {
  service_env_vars = {
    for svc_key, svc in var.services : svc_key => {
      AWS_REGION                 = var.aws_region
      SERVER_PORT                = svc.port
      DATASOURCE_URL             = "jdbc:mysql://${local.shared.rds_endpoint}/${var.db_name}"
      DDL_AUTO                   = "update"
      S3_BUCKET                  = aws_s3_bucket.main.id
      S3_REGION                  = var.aws_region
      CORS_ALLOW_ORIGINS         = svc_key == "lms-api" ? "${var.cors_allow_origins},https://report.aura.co.kr" : var.cors_allow_origins
      SMTP_ENABLED               = "true"
      SMTP_HOST                  = var.smtp_host
      SMTP_PORT                  = var.smtp_port
      CLOUDWATCH_METRICS_ENABLED = "true"
      CLOUDWATCH_NAMESPACE       = "SClass/${title(svc_key)}"
      ALIMTALK_ENABLED           = "true"
      ALIMTALK_PLUS_FRIEND_ID    = "@학생부종합전형"
      ALIMTALK_APP_BASE_URL      = var.alimtalk_app_base_url
      JWT_ACCESS_EXP             = var.jwt_access_exp
      JWT_REFRESH_EXP            = var.jwt_refresh_exp
      REPORT_SERVICE_ENABLED     = svc_key == "backoffice-api" ? "true" : "false"
      REPORT_SERVICE_BASE_URL    = svc_key == "backoffice-api" ? var.report_service_base_url : ""
    }
  }

  service_secrets = {
    DATASOURCE_USERNAME  = aws_ssm_parameter.db_username.arn
    DATASOURCE_PASSWORD  = aws_ssm_parameter.db_password.arn
    JWT_SECRET_KEY       = aws_ssm_parameter.jwt_secret_key.arn
    TOKEN_ENCRYPTION_KEY = aws_ssm_parameter.token_encryption_key.arn
    GOOGLE_CLIENT_ID     = aws_ssm_parameter.google_client_id.arn
    KAKAO_CLIENT_ID      = aws_ssm_parameter.kakao_client_id.arn
    KAKAO_APP_ID         = aws_ssm_parameter.kakao_app_id.arn
    SMTP_USERNAME        = aws_ssm_parameter.smtp_username.arn
    SMTP_PASSWORD        = aws_ssm_parameter.smtp_password.arn
    ALIMTALK_ACCESS_KEY  = aws_ssm_parameter.alimtalk_access_key.arn
    ALIMTALK_SERVICE_ID  = aws_ssm_parameter.alimtalk_service_id.arn
    ALIMTALK_SECRET_KEY  = aws_ssm_parameter.alimtalk_secret_key.arn
  }
}

resource "aws_ecs_express_gateway_service" "services" {
  for_each = var.services

  service_name            = "${local.name_prefix}-${each.key}"
  cluster                 = aws_ecs_cluster.main.name
  execution_role_arn      = aws_iam_role.ecs_execution.arn
  infrastructure_role_arn = aws_iam_role.ecs_infrastructure.arn
  task_role_arn           = aws_iam_role.ecs_task.arn
  cpu                     = each.value.cpu
  memory                  = each.value.memory
  health_check_path       = "/actuator/health"

  primary_container {
    image          = "${aws_ecr_repository.services[each.key].repository_url}:latest"
    container_port = tonumber(each.value.port)

    aws_logs_configuration {
      log_group         = aws_cloudwatch_log_group.services[each.key].name
      log_stream_prefix = each.key
    }

    dynamic "environment" {
      for_each = local.service_env_vars[each.key]
      iterator = env
      content {
        name  = env.key
        value = env.value
      }
    }

    dynamic "secret" {
      for_each = local.service_secrets
      iterator = sec
      content {
        name       = sec.key
        value_from = sec.value
      }
    }
  }

  network_configuration {
    subnets         = local.shared.private_subnets
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  scaling_target {
    min_task_count            = each.value.min_size
    max_task_count            = each.value.max_size
    auto_scaling_metric       = "CPU"
    auto_scaling_target_value = 70
  }

  wait_for_steady_state = false

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }
}
