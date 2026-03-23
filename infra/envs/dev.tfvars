environment = "dev"
aws_region  = "ap-northeast-2"
domain      = "aura.co.kr"

# VPC
vpc_cidr             = "10.0.0.0/16"
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]

# RDS
rds_instance_class = "db.t4g.micro"
db_name            = "sclass"

# App Runner (dev: 최소 스펙)
app_runner_cpu    = "512"
app_runner_memory = "1024"

services = {
  supporters-api = {
    port     = "8081"
    min_size = 1
    max_size = 1
  }
  lms-api = {
    port     = "8082"
    min_size = 1
    max_size = 1
  }
  backoffice-api = {
    port     = "8083"
    min_size = 0
    max_size = 1
  }
}

cors_allow_origins = "http://localhost:3000,http://localhost:3100,http://localhost:3200"

# ──────────────────────────────────────
# Secrets: TF_VAR_ 환경변수로 전달하거나 별도 tfvars 사용
# ──────────────────────────────────────
# db_username          = "..."
# db_password          = "..."
# jwt_secret_key       = "..."
# token_encryption_key = "..."
# google_client_id     = "..."
# kakao_client_id      = "..."
