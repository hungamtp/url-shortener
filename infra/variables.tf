variable "aws_region" {
  description = "AWS region (used for LocalStack)"
  type        = string
  default     = "us-east-1"
}

variable "localstack_endpoint" {
  description = "LocalStack endpoint URL"
  type        = string
  default     = "http://localhost:4566"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "local"
}

variable "app_name" {
  description = "Application name prefix for all resources"
  type        = string
  default     = "url-shortener"
}

variable "dynamodb_table_name" {
  description = "DynamoDB table name for URL mappings"
  type        = string
  default     = "url_mappings"
}

variable "sqs_queue_name" {
  description = "SQS queue name for analytics events"
  type        = string
  default     = "url-analytics"
}

variable "s3_bucket_name" {
  description = "S3 bucket name for logs and exports"
  type        = string
  default     = "url-shortener-logs"
}

variable "url_ttl_days" {
  description = "Default URL time-to-live in days (0 = no expiry)"
  type        = number
  default     = 30
}
