locals {
  any_port = 0
  https_port = "443"
  any_protocol = "-1"
  tcp_protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
  ssh_user = var.ssh_user
  bastion_port = 22
  my_account = format("arn:aws:iam::%s:root", data.aws_caller_identity.account.account_id)
}

resource "aws_launch_configuration" "asg_launch_config" {
  name              = "${var.stack_name}-${terraform.workspace}-launch-configuration"
  image_id          =  data.aws_ami.centos.id
  instance_type     =  var.github_actions_instance_type
  iam_instance_profile = data.aws_iam_instance_profile.instance_profile.name
  security_groups   = [aws_security_group.github_actions_sg.id]
  associate_public_ip_address = var.associate_public_ip_address
  key_name    = var.ssh_key_name
  user_data   = data.template_cloudinit_config.user_data.rendered
  root_block_device {
    volume_type   = var.evs_volume_type
    volume_size   = var.instance_volume_size
    delete_on_termination = true
  }

  lifecycle {
    create_before_destroy = true
  }

}

resource "aws_autoscaling_group" "asg" {
  name                 = join("-",[var.stack_name,terraform.workspace,var.asg_name])
  max_size = var.max_size
  min_size = var.min_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = data.terraform_remote_state.network.outputs.public_subnets_ids
  launch_configuration = aws_launch_configuration.asg_launch_config.name
  health_check_type    =  var.health_check_type
  tag {
    key = "Name"
    propagate_at_launch = true
    value = "${var.stack_name}-${terraform.workspace}-${var.asg_name}"
  }
  dynamic "tag" {
    for_each = var.tags
    content {
      key = tag.key
      value = tag.value
      propagate_at_launch = true
    }
  }
}

resource "aws_autoscaling_schedule" "shutdown" {
  autoscaling_group_name = aws_autoscaling_group.asg.name
  scheduled_action_name  = "bento-auto-stop"
  recurrence             = var.shutdown_schedule
  desired_capacity       = 0
}

resource "aws_autoscaling_schedule" "startup" {
  autoscaling_group_name = aws_autoscaling_group.asg.name
  scheduled_action_name  = "bento-auto-start"
  recurrence             = var.startup_schedule
  desired_capacity       = var.desired_ec2_instance_capacity
  min_size               = var.min_size
  max_size               = var.max_size
}

resource "aws_security_group" "github_actions_sg" {
  name = "${var.stack_name}-${terraform.workspace}-github-actions-sg"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s-frontend-sg",var.stack_name,terraform.workspace),
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_bastion" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  security_group_id = aws_security_group.github_actions_sg.id
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_github_actions" {
  from_port = local.https_port
  protocol = local.tcp_protocol
  to_port = local.https_port
  security_group_id = aws_security_group.github_actions_sg.id
  cidr_blocks = ["0.0.0.0/0"]
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.github_actions_sg.id
  type = "egress"
}

#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "github_actions_ssm_doc" {
  name          = "${var.stack_name}-${terraform.workspace}-bootstrap-github-actions"
  document_type = "Command"
  document_format = "YAML"
  content = <<DOC
---
schemaVersion: '2.2'
description: State Manager Bootstrap Example
parameters: {}
mainSteps:
- action: aws:runShellScript
  name: configureServer
  inputs:
    runCommand:
    - set -ex
    - cd /tmp
    - rm -rf icdc-devops || true
    - yum -y install git jq
    - amazon-linux-extras install ansible2 -y
    - git clone https://github.com/CBIIT/icdc-devops
    - cd icdc-devops/ansible && git checkout master
    - ansible-playbook github-actions-runner.yml -e pat=${jsondecode(data.aws_secretsmanager_secret_version.github_action_token.secret_string)["github-actions-token"]}
DOC
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,"github-actions","ssm-document")
  },
  var.tags,
  )
}

resource "aws_ssm_association" "bootstrap" {
  name = aws_ssm_document.github_actions_ssm_doc.name
  targets {
    key   = "tag:aws:autoscaling:groupName"
    values = [aws_autoscaling_group.asg.name]
  }
}
