resource "aws_alb_target_group" "alb_target_group" {
  name     = lookup(var.svc_name,"name")
  port     = var.target_port
  protocol = var.target_proto
  vpc_id   = var.vpc_id

  stickiness {
    type            = "lb_cookie"
    cookie_duration = 1800
    enabled         = var.target_group_sticky
  }

  health_check {
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 10
    path                = element(values(var.health_check), 0)
    port                = element(values(var.health_check), 1)
  }

  tags = {
    name = lookup(var.svc_name,"host")
  }
}

# data "aws_alb_listener" "alb_listener" {
#   count             = "${var.pass_in_listener_arn == false ? 1 : 0}"
#   load_balancer_arn = "${var.alb_arn}"
#   port              =  "${var.alb_listener_port}"
#   # depends_on        = ["aws_alb_target_group.alb_target_group"]
# }

# resource "random_integer" "https_priority" {
#   min     = 1
#   max     = 20
#   keepers = {
#     # Generate a new integer each time we switch to a new listener ARN
#     llistener_arn                  = "${data.aws_alb_listener.alb_listener.arn}"
#   }
# }

# locals {
#   listener_arn = "${var.pass_in_listener_arn == false ? data.aws_alb_listener.alb_listener.arn : var.listener_arn}"
# #  listener_arn = "${var.listener_arn}"

# }

# local.priority = "${ 100 + random_integer.https_priority.result}"

#Create ALB Listener Rules
resource "aws_alb_listener_rule" "listener_rule_from_data" {
  depends_on = [aws_alb_target_group.alb_target_group]

  # listener_arn                  = "${data.aws_alb_listener.alb_listener.arn}"
  listener_arn = var.listener_arn

  priority = var.priority
  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.alb_target_group.arn
  }
  condition {
    field  = "host-header"
    values = ["${lookup(var.svc_name,"host")}.${var.domain_name}"]
  }
  condition {
    field  = "path-pattern"
    values = [lookup(var.svc_name,"path")]
  }
}

