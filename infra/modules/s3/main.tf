resource "aws_s3_bucket" "url_shortener_logs" {
  bucket        = var.bucket_name
  force_destroy = true

  tags = {
    Name        = var.bucket_name
    Environment = var.environment
    App         = var.app_name
  }
}

resource "aws_s3_bucket_versioning" "url_shortener_logs_versioning" {
  bucket = aws_s3_bucket.url_shortener_logs.id

  versioning_configuration {
    status = "Enabled"
  }
}

