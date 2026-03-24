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
          DATASOURCE_URL       = "jdbc:mysql://${local.shared.rds_endpoint}/${var.db_name}"
          DDL_AUTO             = var.environment == "dev" ? "update" : "validate"
          S3_BUCKET            = aws_s3_bucket.main.id
          S3_REGION            = var.aws_region
          CORS_ALLOW_ORIGINS   = var.cors_allow_origins
          SSM_PARAMETER_PREFIX = "/sclass/${var.environment}"
          SMTP_HOST            = var.smtp_host
          SMTP_PORT            = var.smtp_port
          JWT_ACCESS_EXP       = var.jwt_access_exp
          JWT_REFRESH_EXP      = var.jwt_refresh_exp
          # 민감 정보(DB 자격증명, JWT, OAuth 등)는 SSM Parameter Store에서 런타임 로드
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
      vpc_connector_arn = local.shared.vpc_connector_arn
    }
  }

  auto_scaling_configuration_arn = aws_apprunner_auto_scaling_configuration_version.services[each.key].arn

  tags = {
    Name = "${local.name_prefix}-${each.key}"
  }
}

# ──────────────────────────────────────
# Custom Domain
# ──────────────────────────────────────
resource "aws_apprunner_custom_domain_association" "services" {
  for_each = var.enable_custom_domain ? var.services : {}

  domain_name          = "${each.key}.${local.domain_suffix}"
  service_arn          = aws_apprunner_service.services[each.key].arn
  enable_www_subdomain = false
}
