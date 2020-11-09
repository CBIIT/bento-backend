
resource "aws_launch_configuration" "asg_launch_config_frontend" {
  name              = "${var.stack_name}-${var.env}-frontend-launch-configuration"
  image_id          =  var.ami
  instance_type     =  var.instance_type
  iam_instance_profile = var.instance_profile
  security_groups   = [aws_security_group.frontend_sg.id]
  associate_public_ip_address = var.associate_public_ip_address
  key_name    = var.ssh_key_name
  user_data   = var.user_data
  root_block_device {
    volume_type   = var.evs_volume_type
    volume_size   = var.instance_volume_size
    delete_on_termination = true
  }

  lifecycle {
    create_before_destroy = true
  }

}

resource "aws_launch_configuration" "asg_launch_config_processor" {
  name              = "${var.stack_name}-${var.env}-processor-launch-configuration"
  image_id          =  var.ami
  instance_type     =  var.instance_type
  iam_instance_profile = var.instance_profile
  security_groups   = [aws_security_group.processor_sg.id]
  associate_public_ip_address = var.associate_public_ip_address
  key_name    = var.ssh_key_name
  user_data   = var.user_data
  root_block_device {
    volume_type   = var.evs_volume_type
    volume_size   = var.instance_volume_size
    delete_on_termination = true
  }

  lifecycle {
    create_before_destroy = true
  }

}

resource "aws_autoscaling_group" "asg_frontend" {
  name                 = join("-",[var.stack_name,var.env,var.asg_name,"asg"])
  max_size = var.max_size
  min_size = var.min_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = var.subnets
  launch_configuration = aws_launch_configuration.asg_launch_config_frontend.name
  target_group_arns    = var.target_group_arn
  health_check_type    =  var.health_check_type
  tag {
    key = "Name"
    propagate_at_launch = true
    value = "${var.stack_name}-${var.asg_name}"
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

resource "aws_autoscaling_group" "asg_processor" {
  name                 = join("-",[var.stack_name,var.env,var.asg_name,"asg"])
  max_size = var.max_size
  min_size = var.min_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = var.subnets
  launch_configuration = aws_launch_configuration.asg_launch_config_processor.name
  health_check_type    =  var.health_check_type
  tag {
    key = "Name"
    propagate_at_launch = true
    value = "${var.stack_name}-${var.asg_name}"
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

resource "aws_security_group" "frontend_sg" {
  name = "${var.stack_name}-${var.env}-frontend-sg"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-frontend-sg",var.stack_name),
  },
  var.tags,
  )
}

#create security group
resource "aws_security_group" "processor_sg" {
  name = "${var.stack_name}-${var.env}-processor-sg"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-processor-sg",var.stack_name),
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_bastion_frontend" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = var.bastion_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_alb" {
  from_port = local.alb_port
  protocol = local.tcp_protocol
  to_port = local.alb_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = var.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound_frontend" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.frontend_sg.id
  type = "egress"
}


#define inbound security group rule
resource "aws_security_group_rule" "inbound_activemq" {
  from_port = local.activemq_port
  protocol = local.tcp_protocol
  to_port = local.activemq_port
  cidr_blocks = var.private_subnets_block
  security_group_id = aws_security_group.processor_sg.id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_bastion_processor" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  security_group_id = aws_security_group.processor_sg.id
  source_security_group_id = var.bastion_security_group_id
  type = "ingress"
}
#define outbound security group rule
resource "aws_security_group_rule" "outbound_all_processor" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.processor_sg.id
  type = "egress"
}

locals {
  activemq_port = 8161
  alb_port = var.alb_port
  bastion_port = 22
  any_port = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
}