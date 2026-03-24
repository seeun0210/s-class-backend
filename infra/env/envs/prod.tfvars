environment = "prod"
aws_region  = "ap-northeast-1"
domain      = "aura.co.kr"

# Database (환경별 DB 이름)
db_name = "sclass_prod"

# App Runner (prod: 기본 스펙)
app_runner_cpu    = "1024"
app_runner_memory = "2048"

services = {
  supporters-api = {
    port     = "8080"
    min_size = 1
    max_size = 3
  }
  lms-api = {
    port     = "8080"
    min_size = 1
    max_size = 3
  }
  backoffice-api = {
    port     = "8080"
    min_size = 1
    max_size = 2
  }
}

cors_allow_origins   = "https://aura.co.kr,https://app.aura.co.kr,https://lms.aura.co.kr,https://backoffice.aura.co.kr"
enable_custom_domain = true

# ──────────────────────────────────────
# Secrets: TF_VAR_ 환경변수로 전달하거나 별도 tfvars 사용
# ──────────────────────────────────────
# db_username          = "sclass_prod_user"
# db_password          = "..."
# jwt_secret_key       = "..."
# token_encryption_key = "..."
# google_client_id     = "..."
# kakao_client_id      = "..."
