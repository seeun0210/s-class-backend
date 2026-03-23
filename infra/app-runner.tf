# ──────────────────────────────────────
# Shared VPC Connector (App Runner → VPC)
# ──────────────────────────────────────
resource "aws_apprunner_vpc_connector" "main" {
  vpc_connector_name = local.name_prefix

  subnets         = module.vpc.private_subnets
  security_groups = [aws_security_group.app_runner.id]

  tags = {
    Name = "${local.name_prefix}-vpc-connector"
  }
}

# ──────────────────────────────────────
# Auto Scaling Configuration
# ──────────────────────────────────────
resource "aws_apprunner_auto_scaling_configuration_version" "services" {
  for_each = var.services

  auto_scaling_configuration_name = "${local.name_prefix}-${each.key}"
  min_size                        = each.value.min_size
  max_size                        = each.value.max_size
  max_concurrency                 = 100

  tags = {
    Name = "${local.name_prefix}-${each.key}-scaling"
  }
}

# ──────────────────────────────────────
# App Runner Services
# ──────────────────────────────────────
resource "aws_apprunner_service" "services" {
  for_each = var.services

  service_name = "${local.name_prefix}-${each.key}"

  source_configuration {
    auto_deployments_enabled = false

    authentication_configuration {
      access_role_arn = aws_iam_role.app_runner_ecr_access.arn
    }

    image_repository {
      image_configuration {
        port = each.value.port

        runtime_environment_variables = {
          SERVER_PORT          = each.value.port
          DATASOURCE_URL       = "jdbc:mysql://${aws_db_instance.main.endpoint}/${var.db_name}"
          DATASOURCE_USERNAME  = var.db_username
          DATASOURCE_PASSWORD  = var.db_password
          DDL_AUTO             = var.environment == "dev" ? "update" : "validate"
          JWT_SECRET_KEY       = var.jwt_secret_key
          TOKEN_ENCRYPTION_KEY = var.token_encryption_key
          GOOGLE_CLIENT_ID     = var.google_client_id
          KAKAO_CLIENT_ID      = var.kakao_client_id
          S3_BUCKET            = aws_s3_bucket.main.id
          S3_REGION            = var.aws_region
          CORS_ALLOW_ORIGINS   = var.cors_allow_origins
          # S3는 IAM Role 사용하므로 access-key/secret-key 불필요
          # S3_ENDPOINT도 AWS 기본이라 불필요
        }
      }

      image_identifier      = "${aws_ecr_repository.services[each.key].repository_url}:latest"
      image_repository_type = "ECR"
    }
  }

  instance_configuration {
    cpu               = var.app_runner_cpu
    memory            = var.app_runner_memory
    instance_role_arn = aws_iam_role.app_runner_instance.arn
  }

  network_configuration {
    egress_configuration {
      egress_type       = "VPC"
      vpc_connector_arn = aws_apprunner_vpc_connector.main.arn
    }
  }

  auto_scaling_configuration_arn = aws_apprunner_auto_scaling_configuration_version.services[each.key].arn

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }
}

# ──────────────────────────────────────
# Custom Domain (서브도메인 연결)
# ──────────────────────────────────────
resource "aws_apprunner_custom_domain_association" "services" {
  for_each = var.services

  domain_name          = "${each.key}.${var.domain}"
  service_arn          = aws_apprunner_service.services[each.key].arn
  enable_www_subdomain = false
}
