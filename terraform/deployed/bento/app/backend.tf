terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/app/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}
