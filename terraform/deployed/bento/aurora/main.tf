#aurora
module "aurora" {
  source = "../../../modules/aurora"
  env    =  terraform.workspace
  stack_name = var.stack_name
  tags = var.tags
  vpc_id = var.vpc_id
  db_engine_mode = var.db_engine_mode
  db_engine_version = var.db_engine_version
  db_instance_class = var.db_instance_class
  db_engine_type = var.db_engine_type
  master_username = var.master_username
  allowed_ip_blocks = var.allowed_ip_blocks
  db_subnet_ids = var.db_subnet_ids
  database_name = var.database_name
}