# ──────────────────────────────────────
# SSM Parameter Store (민감 정보 관리)
# ──────────────────────────────────────
resource "aws_ssm_parameter" "db_username" {
  name  = "/sclass/${var.environment}/DATASOURCE_USERNAME"
  type  = "SecureString"
  value = var.db_username

  tags = {
    Name = "${local.name_prefix}-ssm-db-username"
  }
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/sclass/${var.environment}/DATASOURCE_PASSWORD"
  type  = "SecureString"
  value = var.db_password

  tags = {
    Name = "${local.name_prefix}-ssm-db-password"
  }
}

resource "aws_ssm_parameter" "jwt_secret_key" {
  name  = "/sclass/${var.environment}/JWT_SECRET_KEY"
  type  = "SecureString"
  value = var.jwt_secret_key

  tags = {
    Name = "${local.name_prefix}-ssm-jwt-secret"
  }
}

resource "aws_ssm_parameter" "token_encryption_key" {
  name  = "/sclass/${var.environment}/TOKEN_ENCRYPTION_KEY"
  type  = "SecureString"
  value = var.token_encryption_key

  tags = {
    Name = "${local.name_prefix}-ssm-token-encryption"
  }
}

resource "aws_ssm_parameter" "google_client_id" {
  name  = "/sclass/${var.environment}/GOOGLE_CLIENT_ID"
  type  = "SecureString"
  value = var.google_client_id

  tags = {
    Name = "${local.name_prefix}-ssm-google-client"
  }
}

resource "aws_ssm_parameter" "kakao_client_id" {
  name  = "/sclass/${var.environment}/KAKAO_CLIENT_ID"
  type  = "SecureString"
  value = var.kakao_client_id

  tags = {
    Name = "${local.name_prefix}-ssm-kakao-client"
  }
}

resource "aws_ssm_parameter" "smtp_username" {
  name  = "/sclass/${var.environment}/SMTP_USERNAME"
  type  = "SecureString"
  value = var.smtp_username

  tags = {
    Name = "${local.name_prefix}-ssm-smtp-username"
  }
}

resource "aws_ssm_parameter" "smtp_password" {
  name  = "/sclass/${var.environment}/SMTP_PASSWORD"
  type  = "SecureString"
  value = var.smtp_password

  tags = {
    Name = "${local.name_prefix}-ssm-smtp-password"
  }
}

resource "aws_ssm_parameter" "alimtalk_access_key" {
  name  = "/sclass/${var.environment}/ALIMTALK_ACCESS_KEY"
  type  = "SecureString"
  value = var.alimtalk_access_key

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-access"
  }
}

resource "aws_ssm_parameter" "alimtalk_service_id" {
  name  = "/sclass/${var.environment}/ALIMTALK_SERVICE_ID"
  type  = "SecureString"
  value = var.alimtalk_service_id

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-service"
  }
}

resource "aws_ssm_parameter" "alimtalk_secret_key" {
  name  = "/sclass/${var.environment}/ALIMTALK_SECRET_KEY"
  type  = "SecureString"
  value = var.alimtalk_secret_key

  tags = {
    Name = "${local.name_prefix}-ssm-alimtalk-secret"
  }
}
