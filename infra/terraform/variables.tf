variable "aws_region" {
  description = "AWSリージョン"
  type        = string
  default     = "ap-northeast-1"
}

variable "environment" {
  description = "環境名 (dev / prod)"
  type        = string
}

variable "project" {
  description = "プロジェクト名（リソース命名プレフィックスに使用）"
  type        = string
  default     = "cloudops"
}

variable "vpc_cidr" {
  description = "VPC CIDR ブロック"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_instance_class" {
  description = "RDS インスタンスクラス"
  type        = string
  default     = "db.t3.micro"
}

variable "backend_cpu" {
  description = "Backend ECS タスク CPU ユニット"
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "Backend ECS タスク メモリ (MiB)"
  type        = number
  default     = 1024
}

variable "frontend_cpu" {
  description = "Frontend ECS タスク CPU ユニット"
  type        = number
  default     = 256
}

variable "frontend_memory" {
  description = "Frontend ECS タスク メモリ (MiB)"
  type        = number
  default     = 512
}
