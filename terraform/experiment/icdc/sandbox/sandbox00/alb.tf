# create s3 bucket for logs

# data "aws_elb_service_account" "alb_s3_log" {}

# module "alb_s3_bucket_log" {
#   source = "../../modules/s3_logs"
#   bucket_name = "${var.stack_name}-alb-log" 
#   account_arn = "${data.aws_elb_service_account.icdc.arn}"
# }

#Create ALB
resource "aws_alb" "alb" {
  name    = "${var.stack_name}-alb"
  subnets = [data.terraform_remote_state.network.outputs.public_subnet_a_id, data.terraform_remote_state.network.outputs.public_subnet_c_id]
  # TF-UPGRADE-TODO: In Terraform v0.10 and earlier, it was sometimes necessary to
  # force an interpolation expression to be interpreted as a list by wrapping it
  # in an extra set of list brackets. That form was supported for compatibility in
  # v0.11, but is no longer supported in Terraform v0.12.
  #
  # If the expression in the following list itself returns a list, remove the
  # brackets to avoid interpretation as a list of lists. If the expression
  # returns a single list item then leave it as-is and remove this TODO comment.
  security_groups            = [data.terraform_remote_state.network.outputs.public_security_id]
  internal                   = false
  idle_timeout               = var.alb_idle_timeout
  load_balancer_type         = "application"
  enable_deletion_protection = false
  tags = {
    Name = "${var.stack_name}-alb"
  }
  # access_logs {    
  #   bucket                      = "${module.alb_s3_bucket_log.s3_bucket_name}"    
  #   prefix                      = "${var.stack_name}-alb" 
  #   enabled                     = true
  # }
}

#create https redirect
resource "aws_alb_listener" "alb_listener_redirect_https" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

data "aws_acm_certificate" "certificate" {
  domain   = "*.essential-dev.com"
  statuses = ["ISSUED"]
}

resource "aws_lb_listener" "alb_listener_https" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = data.aws_acm_certificate.certificate.arn

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "ICDC page is in maintenance ..."
      status_code  = "200"
    }
  }
}

