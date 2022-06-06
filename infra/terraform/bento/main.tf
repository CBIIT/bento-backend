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
}

module "ecs_service_role" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/roles"
  iam_role_name = local.ecs_service_role_name
  iam_policy_description = "ecs service role"
  add_custom_policy = true
  iam_policy_document

}

module "ecs_task_execution_role" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/roles"
  iam_role_name = local.ecs_task_execution_role_name
  custom_policy_name = "ecs-task-execution-policy"
  iam_policy_description = "ecs task execution role"
  trusted_role_services = [
      "ecs-tasks.amazonaws.com"
  ]
  add_custom_policy = true
  custom_role_policy_arns = ["arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"]
  iam_policy_document = data.aws_iam_policy_document.task_execution_policy_document.json

}

#security_groups  = [aws_security_group.app_sg.id,aws_security_group.fargate_sg.id]