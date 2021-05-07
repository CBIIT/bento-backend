
resource "aws_instance" "ppdc_otg" {
  ami          =  data.aws_ami.centos.id
  instance_type     =  var.fronted_instance_type
  iam_instance_profile = aws_iam_instance_profile.ecs-instance-profile.name
  vpc_security_group_ids   = [aws_security_group.ppdc_otg_security_group.id]
  subnet_id  = data.terraform_remote_state.network.outputs.private_subnets_ids[0]
  key_name    =  var.ssh_key_name
  user_data   =  data.template_cloudinit_config.user_data.rendered
  tags = merge(
  {
    Name        = "${var.stack_name}-otg-${var.env}"
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "ppdc_otg_bastion_host_ssh" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  security_group_id = aws_security_group.ppdc_otg_security_group.id
  type = "ingress"
}

resource "aws_security_group_rule" "ppdc_otg_inbound_alb_http" {
  from_port = local.http_port
  protocol = local.tcp_protocol
  to_port = local.http_port
  security_group_id = aws_security_group.ppdc_otg_security_group.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}


#create ppdc-otg security group
resource "aws_security_group" "ppdc_otg_security_group" {
  name = "${var.stack_name}-otg-${var.env}-sg"
  description = "ppdc-otg security group"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ppdc-otg-sg")
  },
  var.tags,
  )
}


resource "aws_security_group_rule" "ppdc_otg_all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.ppdc_otg_security_group.id
  type = "egress"
}




#create alb target group
resource "aws_lb_target_group" "ppdc_otg_target_group" {
  name = "${var.stack_name}-otg-${var.env}-target-group"
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
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"otg-alb-target")
  },
  var.tags,
  )
}

resource "aws_lb_listener_rule" "ppdc_otg_alb_listener" {
  listener_arn = module.alb.alb_https_listener_arn
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.ppdc_otg_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.stack_name}-otg-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }

}

resource "aws_lb_target_group_attachment" "ppdc_otg" {
  target_group_arn = aws_lb_target_group.ppdc_otg_target_group.arn
  target_id = aws_instance.ppdc_otg.id
  port      = local.http_port
}

#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "ppdc_otg_boostrap" {
  name          = "${var.stack_name}-initialize-otg-${var.env}"
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

resource "aws_ssm_association" "ppdc_otg_ssm" {
  name = aws_ssm_document.ppdc_otp_boostrap.name
  targets {
    key   = "tag:Name"
    values = ["${var.stack_name}-otg-${var.env}"]
  }
}

data "aws_route53_zone" "ppdc_otg_zone" {
  name  = var.domain_name
}

resource "aws_route53_record" "ppdc_otg_records" {
  name = "${var.stack_name}-otg-${var.env}"
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}