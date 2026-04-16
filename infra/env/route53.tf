# ──────────────────────────────────────
# Route 53 Records
# ──────────────────────────────────────

# Prod: ALB ALIAS records
resource "aws_route53_record" "services" {
  for_each = local.is_prod ? var.services : {}

  zone_id = local.shared.route53_zone_id
  name    = "${each.key}.${local.domain_suffix}"
  type    = "A"

  alias {
    name                   = aws_lb.main[0].dns_name
    zone_id                = aws_lb.main[0].zone_id
    evaluate_target_health = true
  }
}

# Dev: EC2 A record (wildcard)
resource "aws_route53_record" "dev_wildcard" {
  count = local.is_prod ? 0 : 1

  zone_id = local.shared.route53_zone_id
  name    = "*.${local.domain_suffix}"
  type    = "A"
  ttl     = 300
  records = [aws_instance.dev[0].public_ip]
}
