resource "aws_dynamodb_table" "url_mappings" {
  name         = var.table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "code"

  attribute {
    name = "code"
    type = "S"
  }

  attribute {
    name = "originalUrl"
    type = "S"
  }

  attribute {
    name = "createdAt"
    type = "S"
  }

  # Global Secondary Index: look up by original URL
  global_secondary_index {
    name            = "originalUrl-index"
    hash_key        = "originalUrl"
    projection_type = "ALL"
  }

  # Global Secondary Index: time-based queries
  global_secondary_index {
    name            = "createdAt-index"
    hash_key        = "createdAt"
    projection_type = "ALL"
  }

  # TTL for auto-expiring short URLs
  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }

  point_in_time_recovery {
    enabled = false # disabled for local dev
  }

  tags = {
    Name        = var.table_name
    Environment = var.environment
    App         = var.app_name
  }
}
