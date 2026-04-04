# ──────────────────────────────────────
# ECS
# ──────────────────────────────────────
output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
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
