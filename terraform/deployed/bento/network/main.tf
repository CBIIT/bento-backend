provider "aws" {
  profile = var.profile
  region = var.region
}
#set the backend for state file
terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/network/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}

module "vpc" {
  source = "../../../modules/networks/vpc"
  stack_name = var.stack_name
  env = var.env
  availability_zones = var.availability_zones
  private_subnets = var.private_subnets
  public_subnets = var.public_subnets
  vpc_cidr_block = var.vpc_cidr_block
  tags = var.tags
}