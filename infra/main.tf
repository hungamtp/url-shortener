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

module "vpc" {
  source = "./modules/vpc"

  app_name           = var.app_name
  environment        = var.environment
  availability_zones = ["${var.aws_region}a", "${var.aws_region}b"]
}

module "alb" {
  source = "./modules/alb"

  app_name        = var.app_name
  environment     = var.environment
  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.public_subnet_ids
  security_groups = [module.vpc.alb_sg_id]
}

module "ecs" {
  source = "./modules/ecs"

  app_name           = var.app_name
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  subnets            = module.vpc.public_subnet_ids
  ecs_sg_id          = module.vpc.ecs_sg_id
  target_group_arn   = module.alb.target_group_arn
  dynamodb_table_arn = module.dynamodb.table_arn
  sqs_queue_arn      = module.sqs.queue_arn
  redis_host         = var.redis_host
  redis_port         = "6379"
  aws_region         = var.aws_region
}
