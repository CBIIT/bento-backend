provider "aws" {
  region = var.region
  profile = var.profile
}
locals {
  bastion_port = 22
  alb_port = var.alb_port
  any_port = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
}

#set the backend for state file
terraform {
  backend "s3" {
    bucket = "comets-terraform-state"
    key = "comets/qa/app/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}


#create alb target group
resource "aws_lb_target_group" "alb_target" {
  name = "${var.stack_name}-test-target-group"
  port = var.app_port
  protocol = "HTTP"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  stickiness {
    type = "lb_cookie"
    cookie_duration = 1800
    enabled = true
  }
  health_check {
    path = "/"
    protocol = "HTTP"
    matcher = "200"
    interval = 15
    timeout = 3
    healthy_threshold = 2
    unhealthy_threshold = 2
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"app-qa-target")
  },
  var.tags,
  )
}

resource "aws_lb_listener_rule" "alb_listener" {
  listener_arn = data.terraform_remote_state.network.outputs.alb_https_listerner_arn
  priority = var.alb_rule_priority
//  condition {
//    field = "host-header"
//    values = ["${var.domain_name}"]
//  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.alb_target.arn
  }
}
