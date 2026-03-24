# ──────────────────────────────────────
# App Runner Service CNAME Records
# ──────────────────────────────────────
resource "aws_route53_record" "app_runner_cname" {
  for_each = var.enable_custom_domain ? var.services : {}

  zone_id = local.shared.route53_zone_id
  name    = "${each.key}.${local.domain_suffix}"
  type    = "CNAME"
  ttl     = 300
  records = [aws_apprunner_service.services[each.key].service_url]
}

# ──────────────────────────────────────
# App Runner Certificate Validation Records
# ──────────────────────────────────────
resource "aws_route53_record" "app_runner_validation" {
  for_each = {
    for item in flatten([
      for key, domain in aws_apprunner_custom_domain_association.services : [
        for record in domain.certificate_validation_records : {
          id    = "${key}-${record.name}"
          name  = record.name
          type  = record.type
          value = record.value
        }
      ]
    ]) : item.id => item
  }

  zone_id = local.shared.route53_zone_id
  name    = each.value.name
  type    = each.value.type
  ttl     = 300
  records = [each.value.value]
}
