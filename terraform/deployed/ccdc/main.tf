terraform {
  required_version = ">= 0.12"
}

provider "aws" {
  profile = var.profile
  shared_credentials_file = "~/.aws/credentials"
  region = var.region
}
#set the backend for state file
terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/ppdc/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}


