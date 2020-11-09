terraform {
  backend "s3" {
    bucket         = "icdc-terraform-state"
    key            = "state/dev/terraform_alb.tfstate"
    dynamodb_table = "icdc-dev-terraform-state-lock"
    encrypt        = "true"
    region         = "us-east-1"
  }
}

