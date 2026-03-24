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
    key            = "sclass/shared/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "sclass-terraform-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = "sclass"
      Layer     = "shared"
      ManagedBy = "terraform"
    }
  }
}

locals {
  name_prefix = "sclass"
  azs         = ["${var.aws_region}a", "${var.aws_region}c"]
}
