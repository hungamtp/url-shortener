# The DynamoDB module creates our primary persistent store.
# It returns the table_arn which we later pass to the ECS module for IAM permissions.
module "dynamodb" {
  source = "./modules/dynamodb"

  table_name  = var.dynamodb_table_name
  environment = var.environment
  app_name    = var.app_name
}

# The SQS module creates the analytics queue and its corresponding Dead Letter Queue (DLQ).
module "sqs" {
  source = "./modules/sqs"

  queue_name  = var.sqs_queue_name
  environment = var.environment
  app_name    = var.app_name
}

# The S3 module creates a bucket for logs and backups.
module "s3" {
  source = "./modules/s3"

  bucket_name = var.s3_bucket_name
  environment = var.environment
  app_name    = var.app_name
}

# The VPC module sets up the networking foundation (VPC, Subnets, Routing, Security Groups).
# This is required before we can deploy the Load Balancer or ECS tasks.
module "vpc" {
  source = "./modules/vpc"

  app_name           = var.app_name
  environment        = var.environment
  availability_zones = ["${var.aws_region}a", "${var.aws_region}b"]
}

# The ALB (Application Load Balancer) module handles distributing incoming traffic.
# It depends on the VPC subnets and VPC ID.
module "alb" {
  source = "./modules/alb"

  app_name        = var.app_name
  environment     = var.environment
  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.public_subnet_ids
  security_groups = [module.vpc.alb_sg_id]
}

# The ECS module deploys the actual Spring Boot application as a Fargate service.
# It wires together the networking (subnets/SG), the load balancer (target group),
# and the data stores (DynamoDB/SQS) by passing their ARNs for IAM role generation.
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
