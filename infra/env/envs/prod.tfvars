environment = "prod"
aws_region  = "ap-northeast-2"
domain      = "sclass.click"

# Database
db_name              = "sclass_prod"
# ECS (prod)
ecs_cpu    = "1024"
ecs_memory = "2048"

services = {
  supporters-api = {
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

cors_allow_origins    = "https://aura.co.kr,https://app.aura.co.kr,https://lms.aura.co.kr,https://backoffice.aura.co.kr,https://sclass.aura.co.kr,https://academy.aura.co.kr"
alimtalk_app_base_url = "https://sclass.aura.co.kr"
frontend_url          = "https://sclass.aura.co.kr"
report_service_base_url          = "https://report-service-452628026107.asia-northeast3.run.app"
report_service_callback_base_url = "https://supporters-api.sclass.click"

# SMTP
smtp_host = "smtp.gmail.com"
smtp_port = "587"

# JWT Expiry
jwt_access_exp  = "3600"
jwt_refresh_exp = "604800"
