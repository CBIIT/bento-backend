#collect details from bastion
data "aws_caller_identity" "account" {}

data "aws_ami" "app" {
 owners = ["924184629216"]

  filter {
    name   = "name"
    values = ["comets-test-ami"]
  }
}

data "terraform_remote_state" "bastion" {
  backend = "s3"
  config = {
    bucket = "comets-terraform-state"
    key = "comets/network/basition/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}
#roles data
data "terraform_remote_state" "roles" {
  backend = "s3"
  config = {
    bucket = "comets-terraform-state"
    key = "comets/deployed/global/roles/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}

#collect details from globals
data "terraform_remote_state" "globals" {
  backend = "s3"
  config = {
    bucket = "comets-terraform-state"
    key = "comets/deployed/global/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}
#grab vpc and other details
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "comets-terraform-state"
    key = "comets/network/vpc/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}
#grab public ssh key
data "aws_ssm_parameter" "sshkey" {
  name = "ssh_public_key"
}

#define user data
data "template_cloudinit_config" "user_data" {
  gzip          = false
  base64_encode = false
  part {
    content = <<EOF
#cloud-config
---
users:
  - name: "${var.devops_user}"
    gecos: "${var.devops_user}"
    sudo: ALL=(ALL) NOPASSWD:ALL
    groups: wheel
    shell: /bin/bash
    ssh_authorized_keys:
    - "${data.aws_ssm_parameter.sshkey.value}"
EOF
  }

  part {
    content_type = "text/x-shellscript"
    content      = "${file("ssm.sh")}"
  }
}

data "aws_subnet" "az" {
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  availability_zone = var.availability_zone
  filter {
    name = "tag:Name"
    values = ["${var.stack_name}-private-${var.availability_zone}"]
  }
}