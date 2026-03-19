# ─── Dead-Letter Queue ────────────────────────────────────────────────────────
resource "aws_sqs_queue" "url_analytics_dlq" {
  name                       = "${var.queue_name}-dlq"
  message_retention_seconds  = 1209600 # 14 days
  visibility_timeout_seconds = 30

  tags = {
    Name        = "${var.queue_name}-dlq"
    Environment = var.environment
    App         = var.app_name
  }
}

# ─── Main Analytics Queue ─────────────────────────────────────────────────────
resource "aws_sqs_queue" "url_analytics" {
  name                       = var.queue_name
  visibility_timeout_seconds = 30
  message_retention_seconds  = 86400 # 1 day
  receive_wait_time_seconds  = 10    # long polling

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.url_analytics_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = var.queue_name
    Environment = var.environment
    App         = var.app_name
  }
}
