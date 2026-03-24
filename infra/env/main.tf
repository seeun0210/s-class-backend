terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

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

# ──────────────────────────────────────
# Shared 인프라 참조 (VPC, RDS, Route53 등)
# ──────────────────────────────────────
data "terraform_remote_state" "shared" {
  backend = "s3"
  config = {
    bucket = "sclass-terraform-state"
    key    = "sclass/shared/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

data "aws_caller_identity" "current" {}

locals {
  name_prefix = "sclass-${var.environment}"
  shared      = data.terraform_remote_state.shared.outputs
}
