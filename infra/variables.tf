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
  default     = "aura.co.kr"
}

# ──────────────────────────────────────
# VPC
# ──────────────────────────────────────
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDRs"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

# ──────────────────────────────────────
# RDS
# ──────────────────────────────────────
variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "sclass"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
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
  default = {
    supporters-api = {
      port     = "8081"
      min_size = 1
      max_size = 3
    }
    lms-api = {
      port     = "8082"
      min_size = 1
      max_size = 3
    }
    backoffice-api = {
      port     = "8083"
      min_size = 0
      max_size = 2
    }
  }
}

# ──────────────────────────────────────
# Application Secrets (TF_VAR_ or tfvars)
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

variable "cors_allow_origins" {
  description = "Comma-separated CORS origins"
  type        = string
}
