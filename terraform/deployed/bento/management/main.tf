provider "aws" {
  profile = var.profile
  region = var.region
}

terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/management/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}
