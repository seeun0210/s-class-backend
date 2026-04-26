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
  default     = "ap-northeast-2"
}

variable "domain" {
  description = "Root domain"
  type        = string
  default     = "sclass.click"
}

# ──────────────────────────────────────
# Services
# ──────────────────────────────────────
variable "services" {
  description = "Service configuration"
  type = map(object({
    port     = string
    min_size = number
    max_size = number
  }))
}

# ──────────────────────────────────────
# ECS (prod)
# ──────────────────────────────────────
variable "ecs_cpu" {
  description = "ECS task vCPU (256, 512, 1024, 2048, 4096)"
  type        = string
  default     = "1024"
}

variable "ecs_memory" {
  description = "ECS task memory in MB"
  type        = string
  default     = "2048"
}

# ──────────────────────────────────────
# EC2 (dev)
# ──────────────────────────────────────
variable "dev_ec2_instance_type" {
  description = "EC2 instance type for dev docker-compose"
  type        = string
  default     = "t4g.small"
}

variable "dev_ec2_key_name" {
  description = "SSH key pair name for dev EC2"
  type        = string
  default     = ""
}

variable "dev_ec2_domain" {
  description = "Dev EC2 domain for certbot SSL (e.g. dev.aura.co.kr)"
  type        = string
  default     = ""
}

variable "dev_certbot_email" {
  description = "Email for Let's Encrypt certbot registration"
  type        = string
  default     = ""
}

# ──────────────────────────────────────
# Database
# ──────────────────────────────────────
variable "db_name" {
  description = "Environment-specific database name"
  type        = string
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

# ──────────────────────────────────────
# CORS
# ──────────────────────────────────────
variable "cors_allow_origins" {
  description = "Comma-separated CORS origins"
  type        = string
}

# ──────────────────────────────────────
# Application Secrets
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

variable "google_client_secret" {
  type      = string
  default   = ""
  sensitive = true
}

variable "google_central_client_id" {
  type    = string
  default = ""
}

variable "google_central_client_secret" {
  type      = string
  default   = ""
  sensitive = true
}

variable "google_calendar_central_enabled" {
  type    = string
  default = "false"
}

variable "google_calendar_central_calendar_id" {
  type    = string
  default = "primary"
}

variable "google_calendar_central_allowed_email" {
  type    = string
  default = ""
}

variable "kakao_client_id" {
  type    = string
  default = ""
}

variable "kakao_app_id" {
  type    = string
  default = ""
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
  type = string
}

variable "report_service_callback_base_url" {
  type    = string
  default = ""
}

variable "report_service_callback_secret" {
  type      = string
  sensitive = true
  default   = ""
}

# ──────────────────────────────────────
# NicePay PG
# ──────────────────────────────────────
variable "nicepay_client_key" {
  type      = string
  sensitive = true
}

variable "nicepay_secret_key" {
  type      = string
  sensitive = true
}

# ──────────────────────────────────────
# Redis
# ──────────────────────────────────────
variable "redis_host" {
  type = string
}

variable "redis_port" {
  type    = string
  default = "6379"
}

variable "redis_password" {
  type      = string
  sensitive = true
}
