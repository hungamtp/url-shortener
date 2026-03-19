output "dynamodb_table_name" {
  description = "DynamoDB table name for URL mappings"
  value       = module.dynamodb.table_name
}

output "dynamodb_table_arn" {
  description = "DynamoDB table ARN"
  value       = module.dynamodb.table_arn
}

output "sqs_queue_url" {
  description = "SQS analytics queue URL"
  value       = module.sqs.queue_url
}

output "sqs_queue_arn" {
  description = "SQS analytics queue ARN"
  value       = module.sqs.queue_arn
}

output "sqs_dlq_url" {
  description = "SQS dead-letter queue URL"
  value       = module.sqs.dlq_url
}

output "s3_bucket_name" {
  description = "S3 bucket name for logs"
  value       = module.s3.bucket_name
}

output "s3_bucket_arn" {
  description = "S3 bucket ARN"
  value       = module.s3.bucket_arn
}
