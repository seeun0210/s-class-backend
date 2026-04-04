# ──────────────────────────────────────
# VPC
# ──────────────────────────────────────
output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnets
}

# ──────────────────────────────────────
# RDS
# ──────────────────────────────────────
output "rds_endpoint" {
  description = "RDS endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

# ──────────────────────────────────────
# Route 53
# ──────────────────────────────────────
output "route53_zone_id" {
  description = "Route 53 hosted zone ID"
  value       = aws_route53_zone.main.zone_id
}

output "nameservers" {
  description = "가비아에 등록할 네임서버 목록"
  value       = aws_route53_zone.main.name_servers
}
