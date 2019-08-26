#set aws provider
provider "aws" {
  region  = "${var.region}"
  profile = "${var.profile}"
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
  vpc_id      = "${data.terraform_remote_state.network.vpc_id}"
  
  # allow SSH from bastion host
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    
    security_groups  = ["${data.terraform_remote_state.network.bastion_security_id}"]
  }
  
  #allow icmp 

  ingress {
    from_port = -1
    to_port = -1
    protocol = "icmp"
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
    Name = "${var.stack_name}-base-sg"
    ByTerraform = "true"
  }
}



