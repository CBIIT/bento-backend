provider "aws" {
  region  = var.region
  profile = var.profile
}

data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket  = "icdc-terraform-state"
    key     = "state/dev/terraform.tfstate"
    encrypt = "true"
    region  = "us-east-1"
  }
}

data "aws_elb_service_account" "icdc" {
}

#create s3 bucket for logs
module "dev_alb_s3_bucket" {
  source      = "../../modules/s3_logs"
  bucket_name = var.s3_bucket_name
  account_arn = data.aws_elb_service_account.icdc.arn
}

module "dev_alb" {
  source              = "../../modules/alb"
  alb_name            = var.alb_name
  alb_security_groups = [data.terraform_remote_state.network.outputs.public_security_id]
  alb_subnets         = [data.terraform_remote_state.network.outputs.public_subnet_id, data.terraform_remote_state.network.outputs.public_subnet_c_id]
  s3_bucket_name      = module.dev_alb_s3_bucket.s3_bucket_name
}

