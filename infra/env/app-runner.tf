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
          AWS_REGION                 = var.aws_region
          SERVER_PORT                = each.value.port
          DATASOURCE_URL             = "jdbc:mysql://${local.shared.rds_endpoint}/${var.db_name}"
          DDL_AUTO                   = "update" # TODO: prod 테이블 생성 후 validate로 복원
          S3_BUCKET                  = aws_s3_bucket.main.id
          S3_REGION                  = var.aws_region
          CORS_ALLOW_ORIGINS         = var.cors_allow_origins
          SMTP_ENABLED               = "true"
          SMTP_HOST                  = var.smtp_host
          SMTP_PORT                  = var.smtp_port
          CLOUDWATCH_METRICS_ENABLED = "true"
          CLOUDWATCH_NAMESPACE       = "SClass/${title(each.key)}"
          ALIMTALK_ENABLED           = "true"
          ALIMTALK_PLUS_FRIEND_ID    = "@학생부종합전형"
          JWT_ACCESS_EXP             = var.jwt_access_exp
          JWT_REFRESH_EXP            = var.jwt_refresh_exp
        }

        runtime_environment_secrets = {
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
