aws_region          = "us-east-1"
localstack_endpoint = "http://localstack:4566"
environment         = "local"
app_name            = "url-shortener"
dynamodb_table_name = "url_mappings"
sqs_queue_name      = "url-analytics"
s3_bucket_name      = "url-shortener-logs"
url_ttl_days        = 30
