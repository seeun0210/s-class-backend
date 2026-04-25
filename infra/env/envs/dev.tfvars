environment = "dev"
aws_region  = "ap-northeast-2"
domain      = "sclass.click"

# Database
db_name = "sclass_dev"

# EC2 (dev: docker-compose + certbot SSL)
dev_ec2_instance_type = "t4g.small"
dev_ec2_domain        = "dev.sclass.click"
dev_certbot_email     = ""  # TODO: Let's Encrypt 등록 이메일 설정

services = {
  supporters-api = {
    port     = "8080"
    min_size = 1
    max_size = 1
  }
  backoffice-api = {
    port     = "8080"
    min_size = 1
    max_size = 1
  }
}

cors_allow_origins      = "http://localhost:3000,http://localhost:3100,http://localhost:3200,https://s-class.dev.aura.co.kr,https://s-class-backoffice.pages.dev,https://report-service-452628026107.asia-northeast3.run.app"
report_service_base_url = "https://report-service-452628026107.asia-northeast3.run.app"
report_service_callback_base_url = "https://supporters-api.dev.sclass.click"
alimtalk_app_base_url = "https://sclass.aura.co.kr"
frontend_url          = "https://sclass.aura.co.kr"

# SMTP
smtp_host = "smtp.gmail.com"
smtp_port = "587"

# JWT Expiry
jwt_access_exp  = "3600"
jwt_refresh_exp = "604800"
