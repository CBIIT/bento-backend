terraform {
  backend "s3" {
    bucket = "icdc-sandbox-terraform-state"
    key = "state/sandbox/terraform.tfstate"
    dynamodb_table = "icdc-sandbox-terraform-state-lock"
    encrypt = "true"
    profile = "icdc"
    region  = "us-east-1"
  }
}
