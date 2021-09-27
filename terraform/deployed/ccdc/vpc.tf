
module "rds-vpc" {
  source = "../../modules/networks/vpc"
  stack_name = var.stack_name
  env = var.env
  availability_zones = var.availability_zones
  private_subnets = var.rds_private_subnets
  public_subnets = var.rds_public_subnets
  vpc_cidr_block = var.rds_vpc_cidr
  tags = var.tags


}