# ──────────────────────────────────────
# RDS (환경별 독립)
# ──────────────────────────────────────

resource "aws_security_group" "rds" {
  name_prefix = "${local.name_prefix}-rds-"
  description = "RDS MySQL for ${var.environment}"
  vpc_id      = local.shared.vpc_id

  tags = {
    Name = "${local.name_prefix}-rds"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "rds_from_nat" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = local.shared.nat_sg_id
  description              = "Admin DB access via NAT"
}

resource "aws_db_subnet_group" "main" {
  name       = "${local.name_prefix}-db"
  subnet_ids = local.shared.private_subnets

  tags = {
    Name = "${local.name_prefix}-db"
  }
}

resource "aws_db_parameter_group" "main" {
  name_prefix = "${local.name_prefix}-mysql-"
  family      = "mysql8.0"
  description = "RDS parameter group for ${var.environment}"

  parameter {
    name         = "max_connections"
    value        = local.is_prod ? "200" : "60"
    apply_method = "pending-reboot"
  }

  tags = {
    Name = "${local.name_prefix}-mysql-params"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_db_instance" "main" {
  identifier = "${local.name_prefix}-mysql"

  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.rds_instance_class

  allocated_storage     = 20
  max_allocated_storage = 50
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  parameter_group_name   = aws_db_parameter_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false
  publicly_accessible = false
  skip_final_snapshot = false

  final_snapshot_identifier = "${local.name_prefix}-mysql-final-snapshot"
  backup_retention_period   = local.is_prod ? 7 : 1

  tags = {
    Name = "${local.name_prefix}-mysql"
  }

  lifecycle {
    ignore_changes = [username, password]
  }
}

locals {
  rds_endpoint = aws_db_instance.main.endpoint
}
