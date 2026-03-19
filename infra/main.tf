module "dynamodb" {
  source = "./modules/dynamodb"

  table_name  = var.dynamodb_table_name
  environment = var.environment
  app_name    = var.app_name
}

module "sqs" {
  source = "./modules/sqs"

  queue_name  = var.sqs_queue_name
  environment = var.environment
  app_name    = var.app_name
}

module "s3" {
  source = "./modules/s3"

  bucket_name = var.s3_bucket_name
  environment = var.environment
  app_name    = var.app_name
}
