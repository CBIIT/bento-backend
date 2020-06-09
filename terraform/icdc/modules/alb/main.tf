#Create ALB
resource "aws_alb" "alb" {
  name                       = var.alb_name
  subnets                    = var.alb_subnets
  security_groups            = var.alb_security_groups
  internal                   = var.internal_alb
  idle_timeout               = var.idle_timeout
  load_balancer_type         = "application"
  enable_deletion_protection = false
  tags = {
    Name = var.alb_name
  }
  access_logs {
    bucket  = var.s3_bucket_name
    prefix  = "icdc_alb"
    enabled = true
  }
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

resource "aws_alb_listener" "alb_listener_redirect_bolt" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "7474"
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "7473"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

