resource "aws_security_group" "neo4j_security_group" {
  name        = "${var.stack_name}-neo4j-sg"
  description = "neo4j security group"
  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id

  ingress {
    from_port = 7474
    to_port   = 7474
    protocol  = "tcp"
    cidr_blocks = [
      "172.18.0.0/16",
    ]
  }

  ingress {
    from_port = 7473
    to_port   = 7473
    protocol  = "tcp"
    cidr_blocks = [
      "172.18.0.0/16",
    ]
  }

  ingress {
    from_port = 7687
    to_port   = 7687
    protocol  = "tcp"
    cidr_blocks = [
      "172.18.0.0/16",
    ]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.stack_name}-neo4j-sg"
    ByTerraform = "true"
  }
}

resource "aws_instance" "neo4j" {
  ami                    = data.terraform_remote_state.network.outputs.centos_ami
  instance_type          = var.neo4j_instance_type
  subnet_id              = data.terraform_remote_state.network.outputs.db_private_subnent_a_id
  key_name               = data.terraform_remote_state.network.outputs.ssh_keypair
  vpc_security_group_ids = [aws_security_group.base_security_group.id, aws_security_group.neo4j_security_group.id]
  private_ip             = var.neo4j_private_ip

  tags = {
    Name        = "${var.stack_name}-neo4j"
    Org         = var.org_name
    ByTerraform = "true"
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

resource "aws_lb_listener" "alb_listener_https_neo4j" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "7473"
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

resource "aws_lb_listener" "alb_listener_https_bolt" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "7687"
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

resource "aws_alb_target_group" "alb_target_group_neo4j" {
  name     = "${var.stack_name}-${var.svc_neo4j}"
  port     = 7473
  protocol = "HTTPS"
  vpc_id   = data.terraform_remote_state.network.outputs.vpc_id

  stickiness {
    type            = "lb_cookie"
    cookie_duration = 1800
    enabled         = true
  }

  health_check {
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 10
    path                = element(values(var.health_check_neo4j), 0)
    port                = element(values(var.health_check_neo4j), 1)
  }

  tags = {
    name = "${var.stack_name}-${var.svc_neo4j}"
  }
}

resource "aws_alb_target_group" "alb_target_group_bolt" {
  name     = "${var.stack_name}-${var.svc_bolt}"
  port     = 7687
  protocol = "HTTPS"
  vpc_id   = data.terraform_remote_state.network.outputs.vpc_id

  stickiness {
    type            = "lb_cookie"
    cookie_duration = 1800
    enabled         = true
  }

  health_check {
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 10
    path                = element(values(var.health_check_neo4j), 0)
    port                = element(values(var.health_check_neo4j), 1)
  }

  tags = {
    name = "${var.stack_name}-${var.svc_bolt}"
  }
}

resource "aws_alb_listener_rule" "listener_rule_neo4j" {
  depends_on   = [aws_alb_target_group.alb_target_group_neo4j]
  listener_arn = aws_lb_listener.alb_listener_https_neo4j.arn
  priority     = var.rule_priority_neo4j
  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.alb_target_group_neo4j.arn
  }
  condition {
    field  = "host-header"
    values = ["${var.svc_neo4j}.${var.domain}"]
  }
}

resource "aws_alb_listener_rule" "listener_rule_bolt" {
  depends_on   = [aws_alb_target_group.alb_target_group_bolt]
  listener_arn = aws_lb_listener.alb_listener_https_bolt.arn
  priority     = var.rule_priority_neo4j
  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.alb_target_group_bolt.arn
  }
  condition {
    field  = "host-header"
    values = ["${var.svc_neo4j}.${var.domain}"]
  }
}

resource "aws_lb_target_group_attachment" "alb_attach_neo4j" {
  target_group_arn = aws_alb_target_group.alb_target_group_neo4j.arn

  target_id = aws_instance.neo4j.id
  port      = "7473"
}

resource "aws_lb_target_group_attachment" "alb_attach_bolt" {
  target_group_arn = aws_alb_target_group.alb_target_group_bolt.arn

  target_id = aws_instance.neo4j.id
  port      = "7687"
}

