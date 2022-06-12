module "alb" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/loadbalancer"
  vpc_id = var.vpc_id
  alb_log_bucket_name = module.s3.bucket_name
  env = terraform.workspace
  certificate_domain_name = var.certificate_domain_name
  acm_certificate_issued_type = local.acm_certificate_issued_type
  internal_alb = var.internal_alb
  alb_security_group_ids = [aws_security_group.alb-sg.id]
  lb_type = var.lb_type
  alb_subnet_ids = local.alb_subnet_ids
  tags = var.tags
  stack_name = var.stack_name
}

module "s3" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/s3"
  bucket_name = local.alb_log_bucket_name
  enable_version = var.enable_version
  bucket_policy = data.aws_iam_policy_document.s3_policy.json
  tags = var.tags
  attach_bucket_policy = var.attach_bucket_policy
  force_destroy_bucket = var.force_destroy_bucket
}

module "ecs" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/ecs"
  stack_name = var.stack_name
  tags = var.tags
  vpc_id = var.vpc_id
  ecs_subnet_ids = var.private_subnet_ids
  application_url = local.application_url
  env = terraform.workspace
  microservices = var.microservices
  alb_https_listener_arn = module.alb.alb_https_listener_arn
  ecs_security_group_ids = [aws_security_group.app_sg.id,aws_security_group.fargate_sg.id]
  ecs_task_role_arn = module.ecs_task_role.iam_role_arn
  ecs_execution_role_arn = module.ecs_task_execution_role.iam_role_arn
}

#create ecr
module "ecr" {
   count = var.create_ecr_repos ? 1: 0
   source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/ecr"
   stack_name = var.stack_name
   ecr_repo_names = var.ecr_repo_names
   tags = var.tags
   env = terraform.workspace
}

#create opensearch
module "opensearch" {
  count = var.create_opensearch_cluster
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/opensearch"
  stack_name = var.stack_name
  tags = var.tags
  vpc_id = var.vpc_id
  opensearch_instance_type = var.opensearch_instance_type
  env = terraform.workspace
  opensearch_subnet_ids = var.private_subnet_ids
  opensearch_version = var.opensearch_version
  opensearch_security_group_ids = [aws_security_group.opensearch_sg[0].id]
}