resource "aws_lb" "alb" {
  name = "${var.stack_name}-${var.nlb_name}"
  load_balancer_type = var.lb_type
  enable_cross_zone_load_balancing = "true"
  subnets = var.subnets
  timeouts {
    create = "10m"
  }

  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,var.nlb_name)
  },
  var.tags,
  )
}

resource "aws_lb_listener" "tcp" {

  load_balancer_arn = aws_lb.alb.arn
  port   = var.nlb_listener_port
  protocol   = local.tcp_protocol
  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.target.arn
  }
}

resource "aws_lb_target_group" "target" {
  name                 = "${var.stack_name}-nlb-target-group"
  port                 = var.nlb_listener_port
  protocol             = local.tcp_protocol
  vpc_id               = var.vpc_id
  target_type          = "ip"
  deregistration_delay = var.deregistration_delay

  health_check {
    protocol            = local.tcp_protocol
    interval            = var.health_check_interval
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }

  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ecs-nlb-target")
  },
  var.tags,
  )
}

# get nlb private ips
data "aws_network_interface" "nlb_ips" {
  count = length(var.subnets)

  filter {
    name   = "description"
    values = ["ELB ${aws_lb.alb.arn_suffix}"]
  }

  filter {
    name   = "subnet-id"
    values = [element(var.subnets, count.index)]
  }
}

locals {
  tcp_protocol = "TCP"
}

