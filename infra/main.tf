terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # ──────────────────────────────────────
  # S3 Backend for state management
  # ──────────────────────────────────────
  # Bootstrap: 아래 버킷과 DynamoDB 테이블은 수동으로 먼저 생성해야 합니다.
  #
  #   aws s3api create-bucket \
  #     --bucket sclass-terraform-state \
  #     --region ap-northeast-2 \
  #     --create-bucket-configuration LocationConstraint=ap-northeast-2
  #
  #   aws s3api put-bucket-versioning \
  #     --bucket sclass-terraform-state \
  #     --versioning-configuration Status=Enabled
  #
  #   aws dynamodb create-table \
  #     --table-name sclass-terraform-lock \
  #     --attribute-definitions AttributeName=LockID,AttributeType=S \
  #     --key-schema AttributeName=LockID,KeyType=HASH \
  #     --billing-mode PAY_PER_REQUEST \
  #     --region ap-northeast-2
  # ──────────────────────────────────────
  backend "s3" {
    bucket         = "sclass-terraform-state"
    region         = "ap-northeast-2"
    dynamodb_table = "sclass-terraform-lock"
    encrypt        = true
    # key는 init 시 -backend-config="key=sclass/<env>/terraform.tfstate" 로 전달
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "sclass"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

locals {
  name_prefix = "sclass-${var.environment}"
  azs         = ["${var.aws_region}a", "${var.aws_region}c"]
}
