# ──────────────────────────────────────
# NAT Instance SG
# ──────────────────────────────────────
resource "aws_security_group" "nat" {
  name_prefix = "${local.name_prefix}-nat-"
  description = "NAT Instance"
  vpc_id      = module.vpc.vpc_id

  # Private Subnet → 인터넷 (HTTP/HTTPS)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.private_subnet_cidrs
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.private_subnet_cidrs
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-nat"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ──────────────────────────────────────
# App Runner VPC Connector SG
# ──────────────────────────────────────
resource "aws_security_group" "app_runner" {
  name_prefix = "${local.name_prefix}-apprunner-"
  description = "App Runner VPC Connector"
  vpc_id      = module.vpc.vpc_id

  # RDS 접근
  egress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.rds.id]
  }

  # 인터넷 접근 (OAuth 등, NAT Instance 경유)
  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-apprunner"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ──────────────────────────────────────
# RDS SG
# ──────────────────────────────────────
resource "aws_security_group" "rds" {
  name_prefix = "${local.name_prefix}-rds-"
  description = "RDS MySQL"
  vpc_id      = module.vpc.vpc_id

  # App Runner에서만 접근 허용
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.app_runner.id]
  }

  tags = {
    Name = "${local.name_prefix}-rds"
  }

  lifecycle {
    create_before_destroy = true
  }
}
