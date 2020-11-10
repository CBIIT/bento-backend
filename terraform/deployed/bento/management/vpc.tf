module "mgt-vpc" {
  source = "../../../modules/networks/vpc"
  stack_name = var.stack_name
  env = var.env
  availability_zones = var.mgt_availability_zones
  private_subnets = var.mgt_private_subnets
  public_subnets = var.mgt_public_subnets
  vpc_cidr_block = var.mgt_vpc_cidr
  tags = var.tags
}