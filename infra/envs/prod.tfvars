environment = "prod"
aws_region  = "ap-northeast-2"
domain      = "aura.co.kr"

# VPC
vpc_cidr             = "10.1.0.0/16"
public_subnet_cidrs  = ["10.1.1.0/24", "10.1.2.0/24"]
private_subnet_cidrs = ["10.1.10.0/24", "10.1.11.0/24"]

# RDS
rds_instance_class = "db.t4g.micro"
db_name            = "sclass"

# App Runner (prod: 기본 스펙)
app_runner_cpu    = "1024"
app_runner_memory = "2048"

services = {
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
    min_size = 1
    max_size = 2
  }
}

cors_allow_origins = "https://aura.co.kr,https://app.aura.co.kr,https://lms.aura.co.kr,https://backoffice.aura.co.kr"

# ──────────────────────────────────────
# Secrets: TF_VAR_ 환경변수로 전달하거나 별도 tfvars 사용
# ──────────────────────────────────────
# db_username          = "..."
# db_password          = "..."
# jwt_secret_key       = "..."
# token_encryption_key = "..."
# google_client_id     = "..."
# kakao_client_id      = "..."
