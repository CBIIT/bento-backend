#set aws provider
provider "aws" {
  #change the region in the variable file. The default region is us-east-1
  region  = "${var.region}"
  #Profile comes from your aws credential. Check your $HOME/.aws/credential
  profile = "${var.profile}"
}

#create the environment vpc
module "sandbox_vpc" {
  source                = "../modules/vpc"
  vpc_region            = "${var.region}" 
  vpc_cidr              = "${var.vpc_cidr}"
  vpc_name              = "icdc_${var.environment}_vpc"            
}

module "sandbox_private_subnet_a_app" {
  source                = "../modules/private_subnet"
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  private_subnet_cidr   = "${var.private_subnet_a_app}"
  subnet_name           = "${var.private_subnet_a_name_app}"
  private_az            = "${var.private_az_a}"
}
module "sandbox_private_subnet_a_db" {
  source                = "../modules/private_subnet"
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  private_subnet_cidr   = "${var.private_subnet_a_db}"
  subnet_name           = "${var.private_subnet_a_name_db}"
  private_az            = "${var.private_az_a}"
}

module "sandbox_private_subnet_c_app" {
  source                = "../modules/private_subnet"
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  private_subnet_cidr   = "${var.private_subnet_c_app}"
  subnet_name           = "${var.private_subnet_c_name_app}"
  private_az            = "${var.private_az_c}"
}
module "sandbox_private_subnet_c_db" {
  source                = "../modules/private_subnet"
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  private_subnet_cidr   = "${var.private_subnet_c_db}"
  subnet_name           = "${var.private_subnet_c_name_db}"
  private_az            = "${var.private_az_c}"
}

#create public subnet a
module "sandbox_public_subnet_a" {
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  source                = "../modules/public_subnet"
  public_az             = "${var.public_az_a}"
  public_subnet_cidr    = "${var.public_subnet_a}"
  subnet_name           = "icdc_${var.environment}_public"
  igw_id                = "${module.sandbox_vpc.igw_id}"
  route_id              = "${module.sandbox_vpc.route_id}"
}

#create public subnet c
module "sandbox_public_subnet_c" {
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  source                = "../modules/public_subnet"
  public_az             = "${var.public_az_c}"
  public_subnet_cidr    = "${var.public_subnet_c}"
  subnet_name           = "icdc_${var.environment}_public"
  igw_id                = "${module.sandbox_vpc.igw_id}"
  route_id              = "${module.sandbox_vpc.route_id}"
}


#create security groups
module "sandbox_security_groups" {
  source                = "../modules/security_groups"
  db_sg_name            = "${var.db_security_group_name}"
  app_sg_name           = "${var.app_security_group_name}"
  docker_sg_name        = "${var.docker_security_group_name}"
  alb_sg_name           = "${var.alb_security_group_name}"
  public_sg_name        = "${var.public_security_group_name}"
  jenkins_sg_name       = "${var.jenkins_security_group_name}"
  bastion_sg_name       = "${var.bastion_security_group_name}"
  vpc_id                = "${module.sandbox_vpc.vpc_id}"
  app_subnet_a          = "${var.private_subnet_a_app}"
  app_subnet_c          = "${var.private_subnet_c_app}"
  public_subnet_a       = "${var.public_subnet_a}"
  public_subnet_c       = "${var.public_subnet_c}"
  vpc_cidr              = "${var.vpc_cidr}"
}

#Search for latest ami
module "centos_ami" {
  source = "../modules/ami"
}
#Upload ssh key
locals {
  icdc_devops_pub = "${secret_resource.devops_ssh_key_public.value}"
}
resource "aws_key_pair" "keypair" {
  
  key_name   = "${var.keyname}"
  public_key = "${local.icdc_devops_pub}"
}

#extract pre assign eip
# module "sandbox_eip" {
#   source = "../modules/eip"
# }
#icdc account details
data "aws_elb_service_account" "icdc" {}

#create s3 bucket for logs
module "sandbox_alb_s3_bucket" {
  source = "../modules/s3_logs"
  bucket_name = "${var.s3_bucket_name}"
  account_arn = "${data.aws_elb_service_account.icdc.arn}"
}
module "sandbox_alb" {
  source = "../modules/alb"
  alb_name  = "${var.alb_name}"
  alb_security_groups = ["${module.sandbox_security_groups.public_security_group_id}"]
  alb_subnets         = ["${module.sandbox_public_subnet_a.public_subnet_id}","${module.sandbox_public_subnet_c.public_subnet_id}"]
  s3_bucket_name      = "${module.sandbox_alb_s3_bucket.s3_bucket_name}"
}

#create s3 bucket to store deployments
resource "aws_s3_bucket" "sandbox_deployment_buckets" {
  bucket = "${var.deployments_bucket_name}"
  acl    = "private"

  tags = {
    Name        = "${var.deployments_bucket_name}"
    Org         = "icdc"
  }
}
#create ami role for s3

module "sandbox_s3_ami_role" {
  source = "../modules/iam"
  
}


