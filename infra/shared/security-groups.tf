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

  # Private Subnet → SMTP (Gmail 587)
  ingress {
    from_port   = 587
    to_port     = 587
    protocol    = "tcp"
    cidr_blocks = var.private_subnet_cidrs
  }

  # Private Subnet → Redis Cloud (dev: 13699, prod: 17870)
  ingress {
    from_port   = 13699
    to_port     = 13699
    protocol    = "tcp"
    cidr_blocks = var.private_subnet_cidrs
  }

  ingress {
    from_port   = 17870
    to_port     = 17870
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
# App Runner VPC Connector SG (dev/prod 공유)
# ──────────────────────────────────────
resource "aws_security_group" "app_runner" {
  name_prefix = "${local.name_prefix}-apprunner-"
  description = "App Runner VPC Connector"
  vpc_id      = module.vpc.vpc_id

  # 인터넷 접근 (OAuth 등, NAT Instance 경유)
  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SMTP 접근 (NAT Instance 경유)
  egress {
    from_port   = 587
    to_port     = 587
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Redis Cloud 접근 (NAT Instance 경유) — dev: 13699, prod: 17870
  egress {
    from_port   = 13699
    to_port     = 13699
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 17870
    to_port     = 17870
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

  tags = {
    Name = "${local.name_prefix}-rds"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ──────────────────────────────────────
# 순환 참조 방지: 별도 rule로 분리
# ──────────────────────────────────────
resource "aws_security_group_rule" "app_runner_to_rds" {
  type                     = "egress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.app_runner.id
  source_security_group_id = aws_security_group.rds.id
}

resource "aws_security_group_rule" "rds_from_app_runner" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = aws_security_group.app_runner.id
}

# ──────────────────────────────────────
# App Runner VPC Connector (dev/prod 공유)
# ──────────────────────────────────────
resource "aws_apprunner_vpc_connector" "main" {
  vpc_connector_name = "${local.name_prefix}-dev"

  subnets         = module.vpc.private_subnets
  security_groups = [aws_security_group.app_runner.id]

  tags = {
    Name = "${local.name_prefix}-vpc-connector"
  }

  lifecycle {
    ignore_changes = [vpc_connector_name]
  }
}
