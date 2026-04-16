# ──────────────────────────────────────
# ECS Execution Role (ECR pull + SSM/CloudWatch Logs)
# ──────────────────────────────────────
resource "aws_iam_role" "ecs_execution" {
  count = local.is_prod ? 1 : 0
  name  = "${local.name_prefix}-ecs-execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution" {
  count      = local.is_prod ? 1 : 0
  role       = aws_iam_role.ecs_execution[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_execution_ssm" {
  count = local.is_prod ? 1 : 0
  name  = "${local.name_prefix}-ecs-exec-ssm"
  role  = aws_iam_role.ecs_execution[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ssm:GetParameter",
        "ssm:GetParameters",
        "ssm:GetParametersByPath"
      ]
      Resource = "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/sclass/${var.environment}/*"
    }]
  })
}

# ──────────────────────────────────────
# ECS Task Role (S3, CloudWatch Metrics)
# ──────────────────────────────────────
resource "aws_iam_role" "ecs_task" {
  count = local.is_prod ? 1 : 0
  name  = "${local.name_prefix}-ecs-task"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_s3" {
  count = local.is_prod ? 1 : 0
  name  = "${local.name_prefix}-s3-access"
  role  = aws_iam_role.ecs_task[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"]
      Resource = [aws_s3_bucket.main.arn, "${aws_s3_bucket.main.arn}/*"]
    }]
  })
}

resource "aws_iam_role_policy" "ecs_task_cloudwatch" {
  count = local.is_prod ? 1 : 0
  name  = "${local.name_prefix}-cloudwatch-put"
  role  = aws_iam_role.ecs_task[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["cloudwatch:PutMetricData"]
      Resource = "*"
    }]
  })
}

# ──────────────────────────────────────
# EC2 Instance Role (dev: S3, SSM, ECR, CloudWatch)
# ──────────────────────────────────────
resource "aws_iam_role" "dev_ec2" {
  count = local.is_prod ? 0 : 1
  name  = "${local.name_prefix}-ec2"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "dev_ec2_s3" {
  count = local.is_prod ? 0 : 1
  name  = "${local.name_prefix}-s3-access"
  role  = aws_iam_role.dev_ec2[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"]
      Resource = [aws_s3_bucket.main.arn, "${aws_s3_bucket.main.arn}/*"]
    }]
  })
}

resource "aws_iam_role_policy" "dev_ec2_ecr" {
  count = local.is_prod ? 0 : 1
  name  = "${local.name_prefix}-ecr-pull"
  role  = aws_iam_role.dev_ec2[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = [for repo in aws_ecr_repository.services : repo.arn]
      }
    ]
  })
}

resource "aws_iam_role_policy" "dev_ec2_ssm" {
  count = local.is_prod ? 0 : 1
  name  = "${local.name_prefix}-ssm-read"
  role  = aws_iam_role.dev_ec2[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = ["ssm:GetParameter", "ssm:GetParameters", "ssm:GetParametersByPath"]
      Resource = "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/sclass/${var.environment}/*"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "dev_ec2_ssm_managed" {
  count      = local.is_prod ? 0 : 1
  role       = aws_iam_role.dev_ec2[0].name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "dev_ec2" {
  count = local.is_prod ? 0 : 1
  name  = "${local.name_prefix}-ec2"
  role  = aws_iam_role.dev_ec2[0].name
}
