
module "ccdc_elasticsearch" {
  source = "../../modules/elasticsearch"

  domain_name = var.domain_name
  profile = var.profile
  region = var.region
  remote_state_bucket_name = var.remote_state_bucket_name
  stack_name = var.stack_name
  tags = var.tags
}