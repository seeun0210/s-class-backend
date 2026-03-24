# ──────────────────────────────────────
# Route 53 Hosted Zone
# ──────────────────────────────────────
resource "aws_route53_zone" "main" {
  name = var.domain

  tags = {
    Name = var.domain
  }
}
