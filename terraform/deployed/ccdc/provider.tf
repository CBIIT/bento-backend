provider "aws" {
  profile = var.profile
  shared_credentials_file = "~/.aws/credentials"
  region = var.region
}
