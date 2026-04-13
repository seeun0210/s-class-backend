# ──────────────────────────────────────
# Environment-Dedicated RDS (prod 전용)
#
# 배경:
#   - 기존 shared RDS(sclass-mysql)를 dev/prod가 공유 → dev 부하로 prod까지 영향
#   - max_connections=60(t4g.micro 기본) 한계로 Connect timed out 다수 발생
#
# 목적:
#   - prod 전용 RDS를 두어 환경 간 격리
#   - custom parameter group으로 max_connections=200 확보
# ──────────────────────────────────────

# 전용 RDS 생성 여부 (prod.tfvars에서 true)
variable "create_dedicated_rds" {
  description = "Create an environment-dedicated RDS instance (separate from shared)"
  type        = bool
  default     = false
}

variable "dedicated_rds_instance_class" {
  description = "Instance class for the dedicated RDS"
  type        = string
  default     = "db.t4g.micro"
}

# ──────────────────────────────────────
# Dedicated RDS SG
# ──────────────────────────────────────
resource "aws_security_group" "dedicated_rds" {
  count = var.create_dedicated_rds ? 1 : 0

  name_prefix = "${local.name_prefix}-rds-"
  description = "Dedicated RDS MySQL for ${var.environment}"
  vpc_id      = local.shared.vpc_id

  tags = {
    Name = "${local.name_prefix}-rds"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "dedicated_rds_from_app_runner" {
  count = var.create_dedicated_rds ? 1 : 0

  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.dedicated_rds[0].id
  source_security_group_id = local.shared.app_runner_sg_id
}

# ──────────────────────────────────────
# Subnet Group
# ──────────────────────────────────────
resource "aws_db_subnet_group" "dedicated" {
  count = var.create_dedicated_rds ? 1 : 0

  name       = "${local.name_prefix}-db"
  subnet_ids = local.shared.private_subnets

  tags = {
    Name = "${local.name_prefix}-db"
  }
}

# ──────────────────────────────────────
# Parameter Group — max_connections 확장
# ──────────────────────────────────────
resource "aws_db_parameter_group" "dedicated" {
  count = var.create_dedicated_rds ? 1 : 0

  name_prefix = "${local.name_prefix}-mysql-"
  family      = "mysql8.0"
  description = "Dedicated RDS parameter group for ${var.environment}"

  parameter {
    name         = "max_connections"
    value        = "200"
    apply_method = "pending-reboot"
  }

  tags = {
    Name = "${local.name_prefix}-mysql-params"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ──────────────────────────────────────
# RDS Instance
# ──────────────────────────────────────
resource "aws_db_instance" "dedicated" {
  count = var.create_dedicated_rds ? 1 : 0

  identifier = "${local.name_prefix}-mysql"

  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.dedicated_rds_instance_class

  allocated_storage     = 20
  max_allocated_storage = 50
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.dedicated[0].name
  parameter_group_name   = aws_db_parameter_group.dedicated[0].name
  vpc_security_group_ids = [aws_security_group.dedicated_rds[0].id]

  multi_az            = false
  publicly_accessible = false
  skip_final_snapshot = false

  final_snapshot_identifier = "${local.name_prefix}-mysql-final-snapshot"
  backup_retention_period   = 7

  tags = {
    Name = "${local.name_prefix}-mysql"
  }

  lifecycle {
    ignore_changes = [username, password]
  }
}

# ──────────────────────────────────────
# Datasource URL (local): 전용 RDS가 있으면 우선 사용
# ──────────────────────────────────────
locals {
  rds_endpoint = var.create_dedicated_rds ? aws_db_instance.dedicated[0].endpoint : local.shared.rds_endpoint
}
