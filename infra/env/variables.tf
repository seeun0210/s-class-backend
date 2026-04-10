# ──────────────────────────────────────
# General
# ──────────────────────────────────────
variable "environment" {
  description = "Environment name (dev / prod)"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-1"
}

variable "domain" {
  description = "Root domain"
  type        = string
  default     = "aura.co.kr"
}

# ──────────────────────────────────────
# App Runner
# ──────────────────────────────────────
variable "app_runner_cpu" {
  description = "App Runner vCPU (256, 512, 1024, 2048, 4096)"
  type        = string
  default     = "1024"
}

variable "app_runner_memory" {
  description = "App Runner memory in MB (512, 1024, 2048, 3072, 4096, ...)"
  type        = string
  default     = "2048"
}

variable "services" {
  description = "App Runner services configuration"
  type = map(object({
    port     = string
    min_size = number
    max_size = number
  }))
}

variable "enable_custom_domain" {
  description = "Enable custom domain for App Runner services"
  type        = bool
  default     = false
}

# ──────────────────────────────────────
# Database (환경별 DB 이름/유저)
# ──────────────────────────────────────
variable "db_name" {
  description = "Environment-specific database name (e.g. sclass_dev)"
  type        = string
}

variable "db_username" {
  description = "Environment-specific database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Environment-specific database password"
  type        = string
  sensitive   = true
}

# ──────────────────────────────────────
# CORS
# ──────────────────────────────────────
variable "cors_allow_origins" {
  description = "Comma-separated CORS origins"
  type        = string
}

# ──────────────────────────────────────
# Application Secrets (SSM에 저장)
# ──────────────────────────────────────
variable "jwt_secret_key" {
  type      = string
  sensitive = true
}

variable "token_encryption_key" {
  type      = string
  sensitive = true
}

variable "google_client_id" {
  type    = string
  default = ""
}

variable "kakao_client_id" {
  type    = string
  default = ""
}

variable "kakao_app_id" {
  description = "Kakao application ID (numeric) for OAuth audience validation"
  type        = string
  default     = ""
}

# ──────────────────────────────────────
# SMTP
# ──────────────────────────────────────
variable "smtp_host" {
  type    = string
  default = "smtp.gmail.com"
}

variable "smtp_port" {
  type    = string
  default = "587"
}

variable "smtp_username" {
  type      = string
  sensitive = true
}

variable "smtp_password" {
  type      = string
  sensitive = true
}

# ──────────────────────────────────────
# JWT Expiry
# ──────────────────────────────────────
variable "jwt_access_exp" {
  type    = string
  default = "3600"
}

variable "jwt_refresh_exp" {
  type    = string
  default = "604800"
}

# ──────────────────────────────────────
# Alimtalk (NCP)
# ──────────────────────────────────────
variable "alimtalk_access_key" {
  type      = string
  sensitive = true
  default   = ""
}

variable "alimtalk_service_id" {
  type    = string
  default = ""
}

variable "alimtalk_secret_key" {
  type      = string
  sensitive = true
  default   = ""
}

variable "alimtalk_app_base_url" {
  type    = string
  default = ""
}

variable "frontend_url" {
  type    = string
  default = "https://sclass.aura.co.kr"
}

# ──────────────────────────────────────
# Report Service
# ──────────────────────────────────────
variable "report_service_base_url" {
  description = "Report service base URL"
  type        = string
}

variable "report_service_callback_base_url" {
  description = "Supporters App Runner base URL (ReportService가 콜백 요청을 보내는 대상)"
  type        = string
  default     = ""
}

variable "report_service_callback_secret" {
  description = "HMAC-SHA256 secret for validating report callbacks"
  type        = string
  sensitive   = true
  default     = ""
}

# ──────────────────────────────────────
# NicePay PG
# ──────────────────────────────────────
variable "nicepay_client_key" {
  description = "NicePay client key"
  type        = string
  sensitive   = true
}

variable "nicepay_secret_key" {
  description = "NicePay secret key"
  type        = string
  sensitive   = true
}

# ──────────────────────────────────────
# Redis (Redis Cloud)
# ──────────────────────────────────────
variable "redis_host" {
  description = "Redis Cloud host"
  type        = string
}

variable "redis_port" {
  description = "Redis Cloud port"
  type        = string
  default     = "6379"
}

variable "redis_password" {
  description = "Redis Cloud password"
  type        = string
  sensitive   = true
}

