# ──────────────────────────────────────
# Outputs
# ──────────────────────────────────────

# Prod: ALB DNS
output "alb_dns_name" {
  description = "ALB DNS name"
  value       = local.is_prod ? aws_lb.main[0].dns_name : null
}

# Dev: EC2 public IP
output "dev_ec2_public_ip" {
  description = "Dev EC2 public IP"
  value       = local.is_prod ? null : aws_instance.dev[0].public_ip
}

# ECR repository URLs
output "ecr_repository_urls" {
  description = "ECR repository URLs"
  value       = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

# RDS endpoint
output "rds_endpoint" {
  description = "RDS endpoint in use"
  value       = local.rds_endpoint
}

# Redis endpoint (prod only)
output "redis_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = local.is_prod ? aws_elasticache_cluster.main[0].cache_nodes[0].address : null
}
