
resource "aws_instance" "ppdc_frontend" {
  ami          =  data.aws_ami.centos.id
  instance_type     =  var.fronted_instance_type
  iam_instance_profile = aws_iam_instance_profile.ecs-instance-profile.name
  vpc_security_group_ids   = [aws_security_group.ppdc_frontend_security_group.id]
  subnet_id  = data.terraform_remote_state.network.outputs.private_subnets_ids[0]
  key_name    =  var.ssh_key_name
  user_data   =  data.template_cloudinit_config.user_data.rendered
  tags = merge(
  {
    Name        = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}"
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "ppdc_frontend_bastion_host_ssh" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  security_group_id = aws_security_group.ppdc_frontend_security_group.id
  type = "ingress"
}

resource "aws_security_group_rule" "ppdc_frontend_inbound_alb_http" {
  from_port = local.http_port
  protocol = local.tcp_protocol
  to_port = local.http_port
  security_group_id = aws_security_group.ppdc_frontend_security_group.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "ppdc_frontend_inbound_alb_playport" {
  from_port = local.play_port
  protocol = local.tcp_protocol
  to_port = local.play_port
  security_group_id = aws_security_group.ppdc_frontend_security_group.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
  #cidr_blocks = [local.vpc_cidr_block]
}


#create ppdc-frontend security group
resource "aws_security_group" "ppdc_frontend_security_group" {
  name = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-sg"
  description = "ppdc-frontend security group"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ppdc-frontend-sg")
  },
  var.tags,
  )
}


resource "aws_security_group_rule" "ppdc_frontend_all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.ppdc_frontend_security_group.id
  type = "egress"
}

#create alb target group
resource "aws_lb_target_group" "ppdc_frontend_target_group" {
  name = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-tg"
  port = local.http_port
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
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"frontend-alb-target")
  },
  var.tags,
  )
}

resource "aws_lb_target_group" "ppdc_frontend_db_target_group" {
  name = "${var.stack_name}-${var.frontend_app_name}-database-${var.env}-tg" # Please change from database to backend
  port = local.play_port
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
    port = local.play_port
    matcher = "200"
    interval = 15
    timeout = 3
    healthy_threshold = 2
    unhealthy_threshold = 2
  }
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"frontend-database-target")
  },
  var.tags,
  )
}

resource "aws_lb_listener_rule" "ppdc_frontend_alb_listener" {
  listener_arn = module.alb.alb_https_listener_arn
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.ppdc_frontend_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.stack_name}-${var.frontend_app_name}-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }

}

resource "aws_lb_listener_rule" "ppdc_frontend_alb_listener_db" {
  listener_arn = module.alb.alb_https_listener_arn
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.ppdc_frontend_db_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.stack_name}-${var.frontend_app_name}-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/graphql/*","/api/graphql"]
    }
  }
}

resource "aws_lb_listener_rule" "ppdc_frontend_alb_listener_apiportal_db" {
  listener_arn = module.alb.alb_https_listener_arn
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.ppdc_frontend_db_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.stack_name}-${var.frontend_app_name}-${var.env}-api.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }
}


resource "aws_lb_target_group_attachment" "ppdc_frontend" {
  target_group_arn = aws_lb_target_group.ppdc_frontend_target_group.arn
  target_id = aws_instance.ppdc_frontend.id
  port      = local.http_port
}

resource "aws_lb_target_group_attachment" "ppdc_frontend_db" {
  target_group_arn = aws_lb_target_group.ppdc_frontend_db_target_group.arn
  target_id = aws_instance.ppdc_frontend.id
  port      = local.play_port
}


#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "ppdc_frontend_boostrap" {
  name          = "${var.stack_name}-${var.frontend_app_name}-initialize-frontend-${var.env}"
  document_type = "Command"
  document_format = "YAML"
  content = <<DOC
---
schemaVersion: '2.2'
description: State Manager Bootstrap Example
parameters: {}
mainSteps:
- action: aws:runShellScript
  name: configureServer
  inputs:
    runCommand:
    - set -ex
    - cd /tmp
    - rm -rf icdc-devops || true
    - yum -y install epel-release
    - yum -y install wget git python-setuptools python-pip
    - pip install --upgrade "pip < 21.0"
    - pip install ansible==2.8.0 boto boto3 botocore
    - git clone https://github.com/CBIIT/icdc-devops
    - cd icdc-devops/ansible && git checkout master
    - ansible-playbook docker.yml
DOC
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ssm-document")
  },
  var.tags,
  )
}

resource "aws_ssm_association" "ppdc_frontend_ssm" {
  name = aws_ssm_document.ppdc_frontend_boostrap.name
  targets {
    key   = "tag:Name"
    values = ["${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}"]
  }
}

data "aws_route53_zone" "ppdc_frontend_zone" {
  name  = var.domain_name
}

resource "aws_route53_record" "ppdc_frontend_records" {
  name = "${var.stack_name}-${var.frontend_app_name}-${var.env}"
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}

resource "aws_route53_record" "ppdc_api_record" {
  name = "${var.stack_name}-${var.frontend_app_name}-${var.env}-api"
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}