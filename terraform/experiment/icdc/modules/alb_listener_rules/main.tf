
data "aws_acm_certificate" "certificate" {
  domain   = "essential-dev.com"
  statuses = ["ISSUED"]
}


resource "aws_alb_target_group" "alb_target_group" {  
  count                         = "${length(keys(var.services_map))}"
  name                          = "${element(keys(var.services_map), count.index)}"
  port                          = "${element(values(var.services_map), count.index)}" 
  protocol                      = "${element(values(var.forward_protocol), count.index)}"  
  vpc_id                        = "${var.vpc_id}"   
  tags {    
    name                        = "${element(keys(var.services_map), count.index)}"    
  }   
  stickiness {    
    type                        = "lb_cookie"    
    cookie_duration             = 1800    
    enabled                     = "${var.target_group_sticky}"  
  }   
  health_check {    
    healthy_threshold           = 3  
    unhealthy_threshold         = 2  
    timeout                     = 5    
    interval                    = 10    
    path                        = "${element(values(var.health_check), 0)}"    
    port                        = "${element(values(var.health_check), 1)}"  
  }
  tags {
    name                        = "${element(keys(var.services_map), count.index)}"
  }
}

resource "random_integer" "priority" {
  count             = "${var.create_listener}"
  min     = 1
  max     = 20
  keepers = {
    # Generate a new integer each time we switch to a new listener ARN
    listener_arn = "${element(aws_lb_listener.alb_listener_https.*.arn,count.index)}" 
  }
}

resource "aws_lb_listener" "alb_listener_https" {
  count                         = "${var.create_listener}"
  load_balancer_arn             = "${var.alb_arn}"  
  port                          = "${element(values(var.listener_port),count.index)}"
  protocol                      = "HTTPS"
  ssl_policy                    = "ELBSecurityPolicy-2016-08"
  certificate_arn               = "${data.aws_acm_certificate.certificate.arn}"

  default_action {
    type                        = "forward"
    target_group_arn            = "${element(aws_alb_target_group.alb_target_group.*.arn,count.index)}"
  }
}


data "aws_alb_listener" "alb_listener" {
  count             = "${1 - var.create_listener}"
  load_balancer_arn = "${var.alb_arn}"
  port              =  "443"
  depends_on        = ["aws_alb_target_group.alb_target_group"]
}


resource "random_integer" "https_priority" {
  count                         = "${1 - var.create_listener}"
  min     = 1
  max     = 20
  keepers = {
    # Generate a new integer each time we switch to a new listener ARN
    llistener_arn                  = "${data.aws_alb_listener.alb_listener.arn}" 
  }
}

#Create ALB Listener Rules
resource "aws_alb_listener_rule" "listener_rule" {
  count                        = "${var.create_listener}"
  depends_on                    = ["aws_alb_target_group.alb_target_group"]  
  listener_arn                  = "${element(aws_lb_listener.alb_listener_https.*.arn,count.index)}" 
  priority                      = "${ 100 + random_integer.priority.result}"   
  action {    
    type                        = "forward"    
    target_group_arn            = "${element(aws_alb_target_group.alb_target_group.*.arn,count.index)}"  
  }   
  condition {    
    field                       = "host-header"    
    values                      = ["${element(keys(var.services_map), count.index)}.${var.domain}"]  
  }
}
  resource "aws_alb_listener_rule" "listener_rule_from_data" {
  count                         = "${1 - var.create_listener}"
  depends_on                    = ["aws_alb_target_group.alb_target_group"]  
  listener_arn                  = "${data.aws_alb_listener.alb_listener.arn}" 
  priority                      = "${ 100 + random_integer.https_priority.result}"     
  action {    
    type                        = "forward"    
    target_group_arn            = "${element(aws_alb_target_group.alb_target_group.*.arn,count.index)}"  
  }   
  condition {    
    field                       = "host-header"    
    values                      = ["${element(keys(var.services_map), count.index)}.${var.domain}"]  
  }
  # condition {
  #   field  = "path-pattern"
  #   values = ["${var.target_group_path}${element(keys(var.services_map), count.index)}"]
  # }
}




# data "aws_instance" "instances_id" {
#   count            = "${length(values(var.services_map))}"
#   instance_tags = {
#     Org  = "icdc"
#   }
#   filter {
#     name   = "tag:Name"
#     values = ["${element(keys(var.services_map), count.index)}"]
#   }
#   depends_on = ["../module.private_subnet"]
# }
# resource "aws_alb_target_group_attachment" "alb_attach" {
#   count            = "${length(values(var.services_map))}"
#   target_group_arn = "${element(aws_alb_target_group.alb_target_group.*.arn,count.index)}"
#   target_id        = "${element(var.instance_id, count.index)}"
#   port             = "${element(values(var.services_map), count.index)}"
# }



#Get key infos
