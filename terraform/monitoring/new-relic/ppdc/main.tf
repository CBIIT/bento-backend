# Configure terraform
/*terraform {
  required_version = "~> 0.13.0"
  required_providers {
    newrelic = {
      source  = "terraform-providers/newrelic"
      version = "~> 2.21.0"
    }
  }
}*/
terraform {
  required_version = ">= 0.13"
}
terraform {
  required_providers {
    newrelic = {
      source  = "newrelic/newrelic"
      version = "2.21.0"
    }
  }
}

# Configure the New Relic provider, values added to environment variable.
provider "newrelic" {}

terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/ppdc/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}


