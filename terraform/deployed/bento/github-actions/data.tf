#grab latest centos ami
data "aws_ami" "centos" {
  most_recent = true

  filter {
    name   = "name"
    values = ["CentOS Linux 7 x86_64 HVM EBS *"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
  owners   = ["679593333241"]
}

#collect details from bastion
data "terraform_remote_state" "bastion" {
  backend = "s3"
  config = {
    bucket = var.remote_state_bucket_name
    key = "bento/management/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}

#grab vpc and other details
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = var.remote_state_bucket_name
    key = "env/qa/bento/network/terraform.tfstate"
    region = var.region
    encrypt = true
  }
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
  - name: "${local.ssh_user}"
    gecos: "${local.ssh_user}"
    sudo: ALL=(ALL) NOPASSWD:ALL
    groups: wheel
    shell: /bin/bash
    ssh_authorized_keys:
    - "${data.aws_ssm_parameter.sshkey.value}"
EOF
  }

  part {
    content_type = "text/x-shellscript"
    content      = file("ssm.sh")
  }
}

data "aws_caller_identity" "account" {
}
#grab public ssh key
data "aws_ssm_parameter" "sshkey" {
  name = "ssh_public_key"
}
data "aws_iam_instance_profile" "instance_profile" {
  name = var.instance_profile_name
}
data "aws_secretsmanager_secret_version" "github_action_token" {
  secret_id = var.github_actions_secret_name
}
data "aws_ssm_parameter" "aws_linux" {
  name = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2"
}