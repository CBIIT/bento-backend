terraform {
  required_version = ">= 0.12"
}

provider "aws" {
  profile = var.profile
  region = var.region
}