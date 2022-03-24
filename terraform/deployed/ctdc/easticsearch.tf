module "ctdc_elasticsearch" {
  source = "./modules/elasticsearch"
  
  stack_name = var.stack_name
  tags = var.tags
  vpc_id = var.vpc_id
  subnet_ip_block = var.subnet_ip_block
  private_subnet_ids = var.private_subnet_ids
  
}
