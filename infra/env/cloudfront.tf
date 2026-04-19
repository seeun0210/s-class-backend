# ──────────────────────────────────────
# Static CDN (CloudFront → S3 public/* prefix)
# ──────────────────────────────────────
# 기존 파일 버킷의 public/* prefix만 CDN을 통해 공개.
# 나머지 경로(학생/교사 서류 등)는 private 유지 (presigned URL).

locals {
  static_domain = "static.${local.domain_suffix}"
}

# ──────────────────────────────────────
# ACM Certificate (us-east-1) — CloudFront 전용
# ──────────────────────────────────────
resource "aws_acm_certificate" "static_cdn" {
  provider = aws.us_east_1

  domain_name       = local.static_domain
  validation_method = "DNS"

  tags = {
    Name = "${local.name_prefix}-static-cdn-cert"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "static_cdn_cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.static_cdn.domain_validation_options :
    dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  zone_id         = local.shared.route53_zone_id
  name            = each.value.name
  type            = each.value.type
  ttl             = 60
  records         = [each.value.record]
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "static_cdn" {
  provider = aws.us_east_1

  certificate_arn         = aws_acm_certificate.static_cdn.arn
  validation_record_fqdns = [for r in aws_route53_record.static_cdn_cert_validation : r.fqdn]
}

# ──────────────────────────────────────
# CloudFront Origin Access Control (OAC)
# ──────────────────────────────────────
resource "aws_cloudfront_origin_access_control" "static_cdn" {
  name                              = "${local.name_prefix}-static-cdn-oac"
  description                       = "OAC for ${local.static_domain}"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# ──────────────────────────────────────
# CloudFront Distribution
# ──────────────────────────────────────
resource "aws_cloudfront_distribution" "static_cdn" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "${local.name_prefix} static assets (public/*)"
  aliases         = [local.static_domain]
  price_class     = "PriceClass_200" # North America + Europe + Asia

  origin {
    domain_name              = aws_s3_bucket.main.bucket_regional_domain_name
    origin_id                = "s3-${aws_s3_bucket.main.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.static_cdn.id
    origin_path              = "/public" # S3 버킷의 public/* 만 노출
  }

  default_cache_behavior {
    target_origin_id       = "s3-${aws_s3_bucket.main.id}"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true

    # AWS Managed Cache Policy: CachingOptimized
    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6"
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.static_cdn.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  tags = {
    Name = "${local.name_prefix}-static-cdn"
  }
}

# ──────────────────────────────────────
# S3 Bucket Policy — CloudFront OAC → public/* 만 허용
# ──────────────────────────────────────
data "aws_iam_policy_document" "main_bucket" {
  statement {
    sid    = "AllowCloudFrontOACReadPublic"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.main.arn}/public/*"]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.static_cdn.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "main" {
  bucket = aws_s3_bucket.main.id
  policy = data.aws_iam_policy_document.main_bucket.json
}

# ──────────────────────────────────────
# Route 53 A alias — static.{domain_suffix} → CloudFront
# ──────────────────────────────────────
resource "aws_route53_record" "static_cdn" {
  zone_id = local.shared.route53_zone_id
  name    = local.static_domain
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.static_cdn.domain_name
    zone_id                = aws_cloudfront_distribution.static_cdn.hosted_zone_id
    evaluate_target_health = false
  }
}
