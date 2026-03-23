# ──────────────────────────────────────
# App Runner
# ──────────────────────────────────────
output "app_runner_urls" {
  description = "App Runner service URLs"
  value = {
    for key, svc in aws_apprunner_service.services :
    key => svc.service_url
  }
}

output "custom_domains" {
  description = "Custom domain → DNS 설정에 필요한 CNAME 레코드"
  value = {
    for key, domain in aws_apprunner_custom_domain_association.services :
    key => {
      domain              = domain.domain_name
      certificate_records = domain.certificate_validation_records
    }
  }
}

# ──────────────────────────────────────
# RDS
# ──────────────────────────────────────
output "rds_endpoint" {
  description = "RDS endpoint"
  value       = aws_db_instance.main.endpoint
}

# ──────────────────────────────────────
# ECR
# ──────────────────────────────────────
output "ecr_repository_urls" {
  description = "ECR repository URLs (docker push 대상)"
  value = {
    for key, repo in aws_ecr_repository.services :
    key => repo.repository_url
  }
}

# ──────────────────────────────────────
# S3
# ──────────────────────────────────────
output "s3_bucket_name" {
  description = "S3 bucket name"
  value       = aws_s3_bucket.main.id
}

# ──────────────────────────────────────
# IAM (GitHub Actions 배포용)
# ──────────────────────────────────────
output "deployer_access_key_id" {
  description = "GitHub Actions deployer access key ID"
  value       = aws_iam_access_key.deployer.id
}

output "deployer_secret_access_key" {
  description = "GitHub Actions deployer secret access key"
  value       = aws_iam_access_key.deployer.secret
  sensitive   = true
}
