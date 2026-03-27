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

resource "aws_iam_role_policy" "app_runner_cloudwatch" {
  name = "${local.name_prefix}-cloudwatch-put"
  role = aws_iam_role.app_runner_instance.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["cloudwatch:PutMetricData"]
        Resource = "*"
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
          "ssm:GetParameter",
          "ssm:GetParameters",
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

resource "aws_iam_policy" "deployer" {
  name = "${local.name_prefix}-deploy-policy"

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
        Sid    = "ECR"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:CreateRepository",
          "ecr:DeleteRepository",
          "ecr:DescribeRepositories",
          "ecr:ListTagsForResource",
          "ecr:TagResource",
          "ecr:UntagResource",
          "ecr:PutLifecyclePolicy",
          "ecr:GetLifecyclePolicy",
          "ecr:GetRepositoryPolicy",
          "ecr:SetRepositoryPolicy",
          "ecr:DeleteLifecyclePolicy"
        ]
        Resource = "*"
      },
      {
        Sid      = "AppRunner"
        Effect   = "Allow"
        Action   = ["apprunner:*"]
        Resource = "*"
      },
      {
        Sid      = "SSMDescribe"
        Effect   = "Allow"
        Action   = ["ssm:DescribeParameters"]
        Resource = "*"
      },
      {
        Sid    = "SSM"
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
          "ssm:GetParametersByPath",
          "ssm:PutParameter",
          "ssm:DeleteParameter",
          "ssm:AddTagsToResource",
          "ssm:ListTagsForResource",
          "ssm:RemoveTagsFromResource"
        ]
        Resource = "arn:aws:ssm:${var.aws_region}:*:parameter/sclass/${var.environment}/*"
      },
      {
        Sid    = "IAM"
        Effect = "Allow"
        Action = [
          "iam:Get*",
          "iam:List*",
          "iam:CreateRole",
          "iam:DeleteRole",
          "iam:PassRole",
          "iam:PutRolePolicy",
          "iam:DeleteRolePolicy",
          "iam:AttachRolePolicy",
          "iam:DetachRolePolicy",
          "iam:TagRole",
          "iam:UntagRole",
          "iam:PutUserPolicy",
          "iam:DeleteUserPolicy",
          "iam:CreateAccessKey",
          "iam:DeleteAccessKey",
          "iam:CreatePolicy",
          "iam:DeletePolicy",
          "iam:CreatePolicyVersion",
          "iam:DeletePolicyVersion",
          "iam:AttachUserPolicy",
          "iam:DetachUserPolicy"
        ]
        Resource = [
          "arn:aws:iam::*:user/${local.name_prefix}-*",
          "arn:aws:iam::*:role/${local.name_prefix}-*",
          "arn:aws:iam::*:policy/${local.name_prefix}-*"
        ]
      },
      {
        Sid    = "S3Infra"
        Effect = "Allow"
        Action = ["s3:*"]
        Resource = [
          "arn:aws:s3:::${local.name_prefix}-*",
          "arn:aws:s3:::${local.name_prefix}-*/*"
        ]
      },
      {
        Sid      = "Route53"
        Effect   = "Allow"
        Action   = ["route53:*"]
        Resource = "*"
      },
      {
        Sid    = "CloudWatch"
        Effect = "Allow"
        Action = [
          "cloudwatch:PutDashboard",
          "cloudwatch:DeleteDashboards",
          "cloudwatch:GetDashboard",
          "cloudwatch:ListDashboards",
          "cloudwatch:PutMetricAlarm",
          "cloudwatch:DeleteAlarms",
          "cloudwatch:DescribeAlarms",
          "cloudwatch:ListTagsForResource",
          "cloudwatch:TagResource",
          "cloudwatch:UntagResource"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_user_policy_attachment" "deployer" {
  user       = aws_iam_user.deployer.name
  policy_arn = aws_iam_policy.deployer.arn
}

resource "aws_iam_access_key" "deployer" {
  user = aws_iam_user.deployer.name
}
