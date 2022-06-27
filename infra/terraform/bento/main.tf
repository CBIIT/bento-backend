module "alb" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/loadbalancer"
  vpc_id = var.vpc_id
  alb_log_bucket_name = module.s3.bucket_name
  env = terraform.workspace
  #certificate_domain_name = var.certificate_domain_name
  #acm_certificate_issued_type = local.acm_certificate_issued_type
  alb_internal = var.internal_alb
  #alb_security_group_ids = [aws_security_group.alb.id]
  alb_type = var.lb_type
  alb_subnet_ids = local.alb_subnet_ids
  tags = var.tags
  stack_name = var.stack_name
  alb_certificate_arn = var.alb_certificate_arn
}

module "s3" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/s3"
  bucket_name = local.alb_log_bucket_name
  #enable_version = var.enable_version
  stack_name = var.stack_name
  env = terraform.workspace
  bucket_policy = data.aws_iam_policy_document.s3_policy.json
  tags = var.tags
  #attach_bucket_policy = var.attach_bucket_policy
  s3_force_destroy = var.s3_force_destroy
}
/*
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
*/

#create opensearch
module "opensearch" {
  count = var.create_opensearch_cluster ? 1: 0
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/opensearch"
  stack_name = var.stack_name
  tags = var.tags
  opensearch_instance_type = var.opensearch_instance_type
  env = terraform.workspace
  opensearch_subnet_ids = var.private_subnet_ids
  opensearch_version = var.opensearch_version
  #opensearch_security_group_ids = [aws_security_group.os.id]
  automated_snapshot_start_hour = 23
  opensearch_ebs_volume_size    = 30
  opensearch_instance_count     = 2
  opensearch_log_type           = "INDEX_SLOW_LOGS"
  opensearch_logs_enabled       = true
  create_os_service_role        = var.create_os_service_role
  multi_az_enabled = var.multi_az_enabled
  vpc_id = var.vpc_id
  opensearch_rollback_on_autotune_disable = "NO_ROLLBACK"
}

/*
module "dns" {
  count = var.create_dns_record ? 1: 0
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/route53"
  env = terraform.workspace
  alb_zone_id = module.alb.alb_zone_id
  alb_dns_name = module.alb.alb_dns_name
  application_subdomain = var.application_subdomain
  domain_name = var.domain_name
}

module "neo4j" {
  count = var.create_db_instance? 1: 0
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/neo4j"
  env = terraform.workspace
  db_security_group_ids = [aws_security_group.database-sg[count.index].id]
  vpc_id = var.vpc_id
  db_subnet_id = var.db_subnet_id
  db_instance_volume_size = var.db_instance_volume_size
  iam_instance_profile_name = var.db_iam_instance_profile_name
  public_ssh_key_ssm_parameter_name = var.public_ssh_key_ssm_parameter_name
  stack_name = var.stack_name
  db_private_ip = var.db_private_ip
  tags = var.tags
}*/