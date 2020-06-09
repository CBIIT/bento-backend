resource "aws_security_group" "appserver_security_group" {
  name        = "${var.stack_name}-appserver-sg"
  description = "appserver security group"
  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id

  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }
  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.docker_agent_security_group.id]
  }
  tags = {
    Name        = "${var.stack_name}-appserver-sg"
    ByTerraform = "true"
  }
}

resource "aws_instance" "app_server1" {
  ami                    = data.terraform_remote_state.network.outputs.centos_ami
  instance_type          = var.appserver_instance_type
  subnet_id              = data.terraform_remote_state.network.outputs.app_private_subnent_a_id
  key_name               = data.terraform_remote_state.network.outputs.ssh_keypair
  vpc_security_group_ids = [aws_security_group.base_security_group.id, aws_security_group.appserver_security_group.id]
  private_ip             = var.appserver_private_ip1

  tags = {
    Name        = "${var.stack_name}-appserver1"
    Org         = var.org_name
    ByTerraform = "true"
  }
}

resource "aws_instance" "app_server2" {
  ami                    = data.terraform_remote_state.network.outputs.centos_ami
  instance_type          = var.appserver_instance_type
  subnet_id              = data.terraform_remote_state.network.outputs.app_private_subnent_a_id
  key_name               = data.terraform_remote_state.network.outputs.ssh_keypair
  vpc_security_group_ids = [aws_security_group.base_security_group.id, aws_security_group.appserver_security_group.id]
  private_ip             = var.appserver_private_ip2

  tags = {
    Name        = "${var.stack_name}-appserver2"
    Org         = var.org_name
    ByTerraform = "true"
  }
}

module "alb_app_config" {
  source       = "../modules/alb_svc_config"
  stack        = var.stack_name
  svc_name     = var.svc_app
  vpc_id       = data.terraform_remote_state.network.outputs.vpc_id
  alb_arn      = aws_alb.alb.arn
  domain_name  = var.domain
  health_check = var.health_check_app
  priority     = var.rule_priority_app
}

data "aws_lb_target_group" "app" {
  name       = "${var.stack_name}-${var.svc_app}"
  depends_on = [module.alb_app_config]
}

resource "aws_lb_target_group_attachment" "alb_attach_app1" {
  target_group_arn = data.aws_lb_target_group.app.arn

  # target_group_arn = "${module.alb_listener_rules_jenkins.alb_target_group_arn}"
  target_id = aws_instance.app_server1.id
  port      = "8080"
}

resource "aws_lb_target_group_attachment" "alb_attach_app2" {
  target_group_arn = data.aws_lb_target_group.app.arn

  # target_group_arn = "${module.alb_listener_rules_jenkins.alb_target_group_arn}"
  target_id = aws_instance.app_server2.id
  port      = "8080"
}

