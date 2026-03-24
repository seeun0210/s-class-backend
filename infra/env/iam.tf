# ──────────────────────────────────────
# App Runner Instance Role (S3, SSM 접근 등)
# ──────────────────────────────────────
resource "aws_iam_role" "app_runner_instance" {
  name = "${local.name_prefix}-apprunner-instance"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "tasks.apprunner.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy" "app_runner_s3" {
  name = "${local.name_prefix}-s3-access"
  role = aws_iam_role.app_runner_instance.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.main.arn,
          "${aws_s3_bucket.main.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy" "app_runner_ssm" {
  name = "${local.name_prefix}-ssm-read"
  role = aws_iam_role.app_runner_instance.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParametersByPath"
        ]
        Resource = "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter/sclass/${var.environment}/*"
      }
    ]
  })
}

# ──────────────────────────────────────
# App Runner ECR Access Role (이미지 pull용)
# ──────────────────────────────────────
resource "aws_iam_role" "app_runner_ecr_access" {
  name = "${local.name_prefix}-apprunner-ecr"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "build.apprunner.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "app_runner_ecr" {
  role       = aws_iam_role.app_runner_ecr_access.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

# ──────────────────────────────────────
# GitHub Actions Deployer (CI/CD용)
# ──────────────────────────────────────
resource "aws_iam_user" "deployer" {
  name = "${local.name_prefix}-deployer"
}

resource "aws_iam_user_policy" "deployer" {
  name = "${local.name_prefix}-deploy-policy"
  user = aws_iam_user.deployer.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "TerraformState"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::sclass-terraform-state",
          "arn:aws:s3:::sclass-terraform-state/*"
        ]
      },
      {
        Sid    = "TerraformLock"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem"
        ]
        Resource = "arn:aws:dynamodb:*:*:table/sclass-terraform-lock"
      },
      {
        Sid    = "ECRAuth"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Sid    = "ECRPush"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        Resource = [for repo in aws_ecr_repository.services : repo.arn]
      },
      {
        Sid    = "AppRunnerList"
        Effect = "Allow"
        Action = [
          "apprunner:ListServices"
        ]
        Resource = "*"
      },
      {
        Sid    = "AppRunnerDeploy"
        Effect = "Allow"
        Action = [
          "apprunner:UpdateService",
          "apprunner:DescribeService",
          "apprunner:StartDeployment"
        ]
        Resource = "arn:aws:apprunner:${var.aws_region}:*:service/${local.name_prefix}-*"
      }
    ]
  })
}

resource "aws_iam_access_key" "deployer" {
  user = aws_iam_user.deployer.name
}
