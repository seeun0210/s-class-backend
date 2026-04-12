# ──────────────────────────────────────
# SSM Parameter Store (민감 정보 관리)
# ──────────────────────────────────────
resource "aws_ssm_parameter" "db_username" {
  name      = "/sclass/${var.environment}/DATASOURCE_USERNAME"
  type      = "SecureString"
  value     = var.db_username
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-db-username"
  }
}

resource "aws_ssm_parameter" "db_password" {
  name      = "/sclass/${var.environment}/DATASOURCE_PASSWORD"
  type      = "SecureString"
  value     = var.db_password
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-db-password"
  }
}

resource "aws_ssm_parameter" "jwt_secret_key" {
  name      = "/sclass/${var.environment}/JWT_SECRET_KEY"
  type      = "SecureString"
  value     = var.jwt_secret_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-jwt-secret"
  }
}

resource "aws_ssm_parameter" "token_encryption_key" {
  name      = "/sclass/${var.environment}/TOKEN_ENCRYPTION_KEY"
  type      = "SecureString"
  value     = var.token_encryption_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-token-encryption"
  }
}

resource "aws_ssm_parameter" "google_client_id" {
  name      = "/sclass/${var.environment}/GOOGLE_CLIENT_ID"
  type      = "SecureString"
  value     = var.google_client_id
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-google-client"
  }
}

resource "aws_ssm_parameter" "kakao_client_id" {
  name      = "/sclass/${var.environment}/KAKAO_CLIENT_ID"
  type      = "SecureString"
  value     = var.kakao_client_id
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-kakao-client"
  }
}

resource "aws_ssm_parameter" "kakao_app_id" {
  name      = "/sclass/${var.environment}/KAKAO_APP_ID"
  type      = "SecureString"
  value     = var.kakao_app_id
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-kakao-app"
  }
}

resource "aws_ssm_parameter" "smtp_username" {
  name      = "/sclass/${var.environment}/SMTP_USERNAME"
  type      = "SecureString"
  value     = var.smtp_username
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-smtp-username"
  }
}

resource "aws_ssm_parameter" "smtp_password" {
  name      = "/sclass/${var.environment}/SMTP_PASSWORD"
  type      = "SecureString"
  value     = var.smtp_password
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-smtp-password"
  }
}

resource "aws_ssm_parameter" "alimtalk_access_key" {
  name      = "/sclass/${var.environment}/ALIMTALK_ACCESS_KEY"
  type      = "SecureString"
  value     = var.alimtalk_access_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-access"
  }
}

resource "aws_ssm_parameter" "alimtalk_service_id" {
  name      = "/sclass/${var.environment}/ALIMTALK_SERVICE_ID"
  type      = "SecureString"
  value     = var.alimtalk_service_id
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-service"
  }
}

resource "aws_ssm_parameter" "alimtalk_secret_key" {
  name      = "/sclass/${var.environment}/ALIMTALK_SECRET_KEY"
  type      = "SecureString"
  value     = var.alimtalk_secret_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-secret"
  }
}

resource "aws_ssm_parameter" "nicepay_client_key" {
  name      = "/sclass/${var.environment}/NICE_PAY_CLIENT_KEY"
  type      = "SecureString"
  value     = var.nicepay_client_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-nicepay-client-key"
  }
}

resource "aws_ssm_parameter" "nicepay_secret_key" {
  name      = "/sclass/${var.environment}/NICE_PAY_SECRET_KEY"
  type      = "SecureString"
  value     = var.nicepay_secret_key
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-nicepay-secret-key"
  }
}

resource "aws_ssm_parameter" "redis_host" {
  name      = "/sclass/${var.environment}/REDIS_HOST"
  type      = "String"
  value     = var.redis_host
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-redis-host"
  }
}

resource "aws_ssm_parameter" "redis_port" {
  name      = "/sclass/${var.environment}/REDIS_PORT"
  type      = "String"
  value     = var.redis_port
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-redis-port"
  }
}

resource "aws_ssm_parameter" "redis_password" {
  name      = "/sclass/${var.environment}/REDIS_PASSWORD"
  type      = "SecureString"
  value     = var.redis_password
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-redis-password"
  }
}

resource "aws_ssm_parameter" "report_service_callback_secret" {
  name      = "/sclass/${var.environment}/REPORT_SERVICE_CALLBACK_SECRET"
  type      = "SecureString"
  value     = var.report_service_callback_secret != "" ? var.report_service_callback_secret : "placeholder"
  overwrite = true

  tags = {
    Name = "${local.name_prefix}-ssm-report-callback-secret"
  }
}
