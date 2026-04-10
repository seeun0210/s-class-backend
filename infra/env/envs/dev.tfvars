environment = "dev"
aws_region  = "ap-northeast-1"
domain      = "aura.co.kr"

# Database (환경별 DB 이름)
db_name = "sclass_dev"

# App Runner (dev: 최소 스펙)
app_runner_cpu    = "512"
app_runner_memory = "1024"

services = {
  supporters-api = {
    port     = "8080"
    min_size = 1
    max_size = 2
  }
  lms-api = {
    port     = "8080"
    min_size = 1
    max_size = 2
  }
  backoffice-api = {
    port     = "8080"
    min_size = 1
    max_size = 2
  }
}

cors_allow_origins      = "http://localhost:3000,http://localhost:3100,http://localhost:3200,https://s-class.dev.aura.co.kr,https://s-class-backoffice.pages.dev,https://report-service-452628026107.asia-northeast3.run.app"
report_service_base_url          = "https://report-service-452628026107.asia-northeast3.run.app"
report_service_callback_base_url = "https://s4n3im3mxc.ap-northeast-1.awsapprunner.com"
alimtalk_app_base_url = "https://sclass.aura.co.kr"
frontend_url          = "https://sclass.aura.co.kr"
enable_custom_domain = false

# SMTP (default: smtp.gmail.com:587)
smtp_host = "smtp.gmail.com"
smtp_port = "587"

# JWT Expiry (default: access=3600, refresh=604800)
jwt_access_exp  = "3600"
jwt_refresh_exp = "604800"

# ──────────────────────────────────────
# Secrets: GitHub Secrets → TF_VAR_ 환경변수로 전달
# ──────────────────────────────────────
# db_username, db_password, jwt_secret_key, token_encryption_key,
# google_client_id, kakao_client_id, kakao_app_id, smtp_username, smtp_password,
# alimtalk_access_key, alimtalk_service_id, alimtalk_secret_key
