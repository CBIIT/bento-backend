
resource "aws_lb" "alb" {

  name               = "${var.stack_name}-${var.alb_name}-${var.env}"
  load_balancer_type = var.lb_type
  subnets            = var.subnets
  security_groups    = [aws_security_group.alb-sg.id]

  access_logs  {
    bucket  = var.alb_s3_bucket_name
    prefix  = "alb-logs"
    enabled = true
  }

  timeouts {
    create = "10m"
  }

  tags = merge(
    {
      "Name" = format("%s-%s", var.stack_name, var.env)
    },
    var.tags,
  )
}

#create alb security group

resource "aws_security_group" "alb-sg" {

  name   = "${var.stack_name}-${var.frontend_app_name}${var.env}-alb-sg"
  vpc_id = var.vpc_id
  tags = merge(
    {
      "Name" = format("%s-%s", var.stack_name, var.env)
    },
    var.tags,
  )
}

resource "aws_security_group_rule" "inbound_http" {

  from_port   = local.http_port
  protocol    = local.tcp_protocol
  to_port     = local.http_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.alb-sg.id
  type              = "ingress"
}

resource "aws_security_group_rule" "inbound_https" {

  from_port   = local.https_port
  protocol    = local.tcp_protocol
  to_port     = local.https_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.alb-sg.id
  type              = "ingress"
}

resource "aws_security_group_rule" "all_outbound" {

  from_port   = local.any_port
  protocol    = local.any_protocol
  to_port     = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.alb-sg.id
  type              = "egress"
}

#create https redirect
resource "aws_lb_listener" "redirect_https" {

  load_balancer_arn = aws_lb.alb.arn
  port              = local.http_port
  protocol          = "HTTP"
  default_action {
    type = "redirect"
    redirect {
      port        = local.https_port
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "listener_https" {

  load_balancer_arn = aws_lb.alb.arn
  port              = local.https_port
  protocol          = "HTTPS"
  ssl_policy        = var.ssl_policy
  certificate_arn   = var.certificate_arn
  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = var.default_message
      status_code  = "200"
    }
  }
}

locals {
  http_port    = 80
  any_port     = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  https_port   = "443"
  all_ips      = ["0.0.0.0/0"]
}

resource "aws_s3_bucket" "alb_logs_bucket" {
  bucket = var.alb_s3_bucket_name
  acl = "private"
  policy = data.aws_iam_policy_document.s3_policy.json
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
  lifecycle_rule {
    id = "transition_to_standard_ia"
    enabled = (var.s3_object_expiration_days - var.s3_object_standard_ia_transition_days > 30)
    transition {
      storage_class = "STANDARD_IA"
      days = var.s3_object_standard_ia_transition_days
    }
    noncurrent_version_transition {
      days = var.s3_object_nonactive_expiration_days - 30 > 30 ? 30 : var.s3_object_nonactive_expiration_days + 30
      storage_class = "STANDARD_IA"
    }
  }
  lifecycle_rule {
    id = "expire_objects"
    enabled = true
    expiration {
      days = var.s3_object_expiration_days
    }
    noncurrent_version_expiration {
      days = var.s3_object_nonactive_expiration_days
    }
  }
}
