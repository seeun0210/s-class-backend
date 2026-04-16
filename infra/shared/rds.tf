# ──────────────────────────────────────
# RDS Subnet Group
# ──────────────────────────────────────
resource "aws_db_subnet_group" "main" {
  name       = "${local.name_prefix}-db"
  subnet_ids = module.vpc.private_subnets

  tags = {
    Name = "${local.name_prefix}-db"
  }
}

# ──────────────────────────────────────
# RDS MySQL (dev 공유용, Single-AZ)
# ──────────────────────────────────────
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
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false
  publicly_accessible = false
  skip_final_snapshot = false

  final_snapshot_identifier = "${local.name_prefix}-final-snapshot"
  backup_retention_period   = 7

  tags = {
    Name = "${local.name_prefix}-mysql"
  }

  lifecycle {
    ignore_changes = [username, password]
  }
}
