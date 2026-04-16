# ──────────────────────────────────────
# ElastiCache Redis (prod only)
# ──────────────────────────────────────

resource "aws_security_group" "redis" {
  count       = local.is_prod ? 1 : 0
  name_prefix = "${local.name_prefix}-redis-"
  description = "ElastiCache Redis for ${var.environment}"
  vpc_id      = local.shared.vpc_id

  tags = {
    Name = "${local.name_prefix}-redis"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "redis_from_ecs" {
  count = local.is_prod ? 1 : 0

  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = aws_security_group.redis[0].id
  source_security_group_id = aws_security_group.ecs_tasks[0].id
  description              = "Redis from ECS Tasks"
}

resource "aws_elasticache_subnet_group" "main" {
  count = local.is_prod ? 1 : 0

  name       = "${local.name_prefix}-redis"
  subnet_ids = local.shared.private_subnets

  tags = {
    Name = "${local.name_prefix}-redis"
  }
}

resource "aws_elasticache_cluster" "main" {
  count = local.is_prod ? 1 : 0

  cluster_id           = "${local.name_prefix}-redis"
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = "cache.t4g.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379

  subnet_group_name  = aws_elasticache_subnet_group.main[0].name
  security_group_ids = [aws_security_group.redis[0].id]

  tags = {
    Name = "${local.name_prefix}-redis"
  }
}
