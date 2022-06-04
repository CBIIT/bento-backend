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
}