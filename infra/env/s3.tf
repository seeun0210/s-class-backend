# ──────────────────────────────────────
# S3 Bucket (파일 업로드용)
# ──────────────────────────────────────
resource "aws_s3_bucket" "main" {
  bucket = "${local.name_prefix}-files"

  tags = {
    Name = "${local.name_prefix}-files"
  }
}

resource "aws_s3_bucket_public_access_block" "main" {
  bucket = aws_s3_bucket.main.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "main" {
  bucket = aws_s3_bucket.main.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# ──────────────────────────────────────
# CORS (Presigned URL 업로드용)
# ──────────────────────────────────────
resource "aws_s3_bucket_cors_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST"]
    allowed_origins = split(",", var.cors_allow_origins)
    max_age_seconds = 3600
  }
}
