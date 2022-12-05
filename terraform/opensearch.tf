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
  automated_snapshot_start_hour =  var.automated_snapshot_start_hour
  opensearch_ebs_volume_size    =  var.opensearch_ebs_volume_size
  opensearch_instance_count     =  var.opensearch_instance_count
  opensearch_log_types           = ["INDEX_SLOW_LOGS"]
  create_os_service_role        = var.create_os_service_role
  multi_az_enabled = var.multi_az_enabled
  vpc_id = var.vpc_id
  opensearch_autotune_rollback_type = "NO_ROLLBACK"
  create_cloudwatch_log_policy = var.create_cloudwatch_log_policy
}