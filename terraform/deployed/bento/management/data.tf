data "aws_acm_certificate" "certificate" {
  domain = "*.bento-tools.org"
  types  = ["AMAZON_ISSUED"]
}

data "aws_ami" "jenkins" {
  owners = [data.aws_caller_identity.account.account_id]

  filter {
    name   = "name"
    values = ["bento-jenkins-latest-ami"]

  }
}
data "aws_caller_identity" "account" {
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
  - name: "${var.ssh_user}"
    gecos: "${var.ssh_user}"
    sudo: ALL=(ALL) NOPASSWD:ALL
    groups: wheel
    shell: /bin/bash
    ssh_authorized_keys:
    - "${data.aws_ssm_parameter.sshkey.value}"
EOF
  }
}

#grab public ssh key
data "aws_ssm_parameter" "sshkey" {
  name = "ssh_public_key"
}

data "aws_ami" "windows_ami" {
  owners      = ["amazon"]
  most_recent = true

  filter {
    name   = "name"
    values = ["Windows_Server-2019-English-Full-Base-2020.10.14"]
  }
}

data "aws_eip" "bastion" {
  tags = {
    Name = "bento-bastion-host"
  }
}

data "aws_eip" "rdp" {
  tags = {
    Name = "katalon-test-vm"
  }
}