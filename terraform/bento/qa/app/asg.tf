
resource "aws_launch_configuration" "asg_launch_config" {
  name              = "${var.stack_name}-${var.env}-app-launch-configuration"
  image_id          =  data.aws_ami.app.id
  instance_type     =  var.app_instance_type
  iam_instance_profile = data.terraform_remote_state.roles.outputs.iam_instance_profile_id
  security_groups   = [aws_security_group.app_sg.id]
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
  name                 = join("-",[var.stack_name,var.env,var.app_asg_name,"asg"])
  max_size = var.max_size
  min_size = var.min_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = data.terraform_remote_state.network.outputs.private_subnets_ids
//  vpc_zone_identifier  = [data.aws_subnet.az.id]
  launch_configuration = aws_launch_configuration.asg_launch_config.name
  target_group_arns    = [aws_lb_target_group.alb_target.arn]
  health_check_type    =  var.health_check_type
  tag {
    key = "Name"
    propagate_at_launch = true
    value = "${var.stack_name}-${var.app_asg_name}"
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

resource "aws_security_group" "app_sg" {
  name = "${var.stack_name}-${var.env}-app-sg"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-app-sg",var.stack_name),
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_bastion" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  security_group_id = aws_security_group.app_sg.id
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_alb" {
  from_port = var.app_port
  protocol = local.tcp_protocol
  to_port = var.app_port
  security_group_id = aws_security_group.app_sg.id
  source_security_group_id = data.terraform_remote_state.network.outputs.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound_app" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.app_sg.id
  type = "egress"
}
