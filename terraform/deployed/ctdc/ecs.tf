module "ctdc_ecs" {
  source = "./modules/ecs"
  
  stack_name = var.stack_name
  tags = var.tags
  container_replicas = 1
  frontend_target_group_arn = "arn:aws:elasticloadbalancing:${data.aws_region.region.name}:${data.aws_caller_identity.caller.account_id}:targetgroup/ctdc-dev-frontend/9a419742c59b9f2a"
  backend_target_group_arn = "arn:aws:elasticloadbalancing:${data.aws_region.region.name}:${data.aws_caller_identity.caller.account_id}:targetgroup/ctdc-dev-backend/10c42d30a50ab009"
  
}
