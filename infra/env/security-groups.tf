# ──────────────────────────────────────
# ECS Tasks SG
# ──────────────────────────────────────
resource "aws_security_group" "ecs_tasks" {
  name_prefix = "${local.name_prefix}-ecs-tasks-"
  description = "ECS Tasks"
  vpc_id      = local.shared.vpc_id

  # 아웃바운드 전체 허용 (RDS, 외부 API 등)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-ecs-tasks"
  }

  lifecycle {
    create_before_destroy = true
  }
}
