# ──────────────────────────────────────
# Dedicated RDS (prod 전용)
# ──────────────────────────────────────

resource "aws_security_group" "dedicated_rds" {
  count       = var.create_dedicated_rds ? 1 : 0
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

resource "aws_security_group_rule" "dedicated_rds_from_nat" {
  count = var.create_dedicated_rds ? 1 : 0

  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.dedicated_rds[0].id
  source_security_group_id = local.shared.nat_sg_id
  description              = "Admin DB access via NAT"
}

resource "aws_db_subnet_group" "dedicated" {
  count = var.create_dedicated_rds ? 1 : 0

  name       = "${local.name_prefix}-db"
  subnet_ids = local.shared.private_subnets

  tags = {
    Name = "${local.name_prefix}-db"
  }
}

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

locals {
  rds_endpoint = var.create_dedicated_rds ? aws_db_instance.dedicated[0].endpoint : local.shared.rds_endpoint
}
