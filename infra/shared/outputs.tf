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
# Security Groups
# ──────────────────────────────────────
output "nat_sg_id" {
  description = "NAT Instance security group ID"
  value       = aws_security_group.nat.id
}

# ──────────────────────────────────────
# Route 53
# ──────────────────────────────────────
output "route53_zone_id" {
  description = "Route 53 hosted zone ID"
  value       = aws_route53_zone.main.zone_id
}

output "nameservers" {
  description = "Route53에서 도메인 구매 시 자동 설정됨"
  value       = aws_route53_zone.main.name_servers
}

