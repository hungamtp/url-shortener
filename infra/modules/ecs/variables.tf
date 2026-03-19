variable "app_name" { type = string }
variable "environment" { type = string }
variable "vpc_id" { type = string }
variable "subnets" { type = list(string) }
variable "ecs_sg_id" { type = string }
variable "target_group_arn" { type = string }

variable "dynamodb_table_arn" { type = string }
variable "sqs_queue_arn" { type = string }

variable "redis_host" { type = string }
variable "redis_port" { type = string }
variable "aws_region" { type = string }

variable "container_image" { 
  type = string 
  default = "url-shortener-service:latest"
}
