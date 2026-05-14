terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # リモートステート（本番運用時に有効化）
  # backend "s3" {
  #   bucket = "cloudops-tfstate"
  #   key    = "cloudops-incident-manager/terraform.tfstate"
  #   region = "ap-northeast-1"
  # }
}

provider "aws" {
  region = var.aws_region
}

# --- モジュール呼び出し（CC-010 で実装） ---

# module "vpc" {
#   source = "../../modules/vpc"
#   ...
# }

# module "rds" {
#   source = "../../modules/rds"
#   ...
# }

# module "ecr" {
#   source = "../../modules/ecr"
#   ...
# }

# module "ecs" {
#   source = "../../modules/ecs"
#   ...
# }

# module "alb" {
#   source = "../../modules/alb"
#   ...
# }
