output "queue_url" {
  description = "SQS analytics queue URL"
  value       = aws_sqs_queue.url_analytics.url
}

output "queue_arn" {
  description = "SQS analytics queue ARN"
  value       = aws_sqs_queue.url_analytics.arn
}

output "dlq_url" {
  description = "SQS dead-letter queue URL"
  value       = aws_sqs_queue.url_analytics_dlq.url
}
