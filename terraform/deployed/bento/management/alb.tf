module "alb" {
  source = "../../../modules/networks/alb"
  stack_name = var.stack_name
  vpc_id = module.mgt-vpc.vpc_id
  certificate_arn = data.aws_acm_certificate.certificate.arn
  subnets = module.mgt-vpc.public_subnets_ids
  tags = var.tags
  env = var.env
}


