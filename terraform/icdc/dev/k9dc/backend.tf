terraform {
  backend "s3" {
    bucket = "icdc-sandbox-terraform-state"
    key = "state/sandbox/terraform_k9dc.tfstate"
    dynamodb_table = "icdc-sandbox-terraform-state-lock"
    encrypt = "true"
    region  = "us-east-1"
  }
}
