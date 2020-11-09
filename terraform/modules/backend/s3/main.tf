terraform {
  backend "s3" {
    bucket = var.remote_state_bucket
    key = var.remote_state_key
    region = "us-east-1"
    encrypt = true
  }
}
