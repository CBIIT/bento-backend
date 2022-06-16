locals {
  cloudfront_log_path_prefix = "${var.stack_name}/cloudfront/logs/annotations"
}
module "cloudfront" {
  source = "../../../modules/cloudfront"
  cloudfront_distribution_bucket_name = var.cloudfront_distribution_bucket_name
  cloudfront_distribution_log_bucket_name = var.cloudfront_distribution_log_bucket_name
  cloudfront_log_path_prefix_key = local.cloudfront_log_path_prefix
  cloudfront_origin_access_identity_description = "cloudfront origin access identity for annotation files"
  env = terraform.workspace
  stack_name = var.stack_name
  tags = var.tags
}