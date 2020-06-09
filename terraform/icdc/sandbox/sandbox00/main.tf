#set aws provider
provider "aws" {
  region  = var.region
  profile = var.profile
}

data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket  = "icdc-sandbox-terraform-state"
    key     = "state/sandbox/terraform.tfstate"
    encrypt = "true"
    region  = "us-east-1"
  }
}

# data "terraform_remote_state" "k9dc" {
#   backend = "s3"
#   config = {
#     bucket  = "icdc-sandbox-terraform-state"
#     key     = "state/sandbox/terraform_k9dc.tfstate"
#     encrypt = "true"
#     region  = "us-east-1"
#   }
# }

locals {
}

resource "aws_security_group" "base_security_group" {
  name        = "${var.stack_name}-base-sg"
  description = "base security group"
  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id

  # allow SSH from bastion host
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"

    # TF-UPGRADE-TODO: In Terraform v0.10 and earlier, it was sometimes necessary to
    # force an interpolation expression to be interpreted as a list by wrapping it
    # in an extra set of list brackets. That form was supported for compatibility in
    # v0.11, but is no longer supported in Terraform v0.12.
    #
    # If the expression in the following list itself returns a list, remove the
    # brackets to avoid interpretation as a list of lists. If the expression
    # returns a single list item then leave it as-is and remove this TODO comment.
    security_groups = [data.terraform_remote_state.network.outputs.bastion_security_id]
  }

  #allow icmp 

  ingress {
    from_port = -1
    to_port   = -1
    protocol  = "icmp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  #allow all outgoing protocols
  egress {
    from_port = "0"
    to_port   = "0"
    protocol  = "-1"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags = {
    Name        = "${var.stack_name}-base-sg"
    ByTerraform = "true"
  }
}

