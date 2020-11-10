
resource "aws_instance" "jenkins" {
  ami          =  data.aws_ami.jenkins.id
  instance_type     =  var.ec2_instance_type
  iam_instance_profile = aws_iam_instance_profile.ecs-instance-profile.name
  vpc_security_group_ids   = [aws_security_group.jenkins-sg.id]
  subnet_id   = module.mgt-vpc.private_subnets_ids[0]
  private_ip = var.jenkins_private_ip
  key_name    =  var.ssh_key_name
  user_data   =  data.template_cloudinit_config.user_data.rendered
  tags = {
    Name        = "${var.stack_name}-jenkins"
  }

}

#create jenkins security group
resource "aws_security_group" "jenkins-sg" {
  name = "${var.stack_name}-jenkins-sg"
  description = "jenkins security group"
  vpc_id = module.mgt-vpc.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"jenkins-sg")
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "docker_http" {
  from_port = local.docker_http
  protocol = local.tcp_protocol
  to_port = local.docker_http
  cidr_blocks = var.mgt_private_subnets
  security_group_id = aws_security_group.jenkins-sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "bastion_host_ssh" {
  from_port = local.ssh
  protocol = local.tcp_protocol
  to_port = local.ssh
  source_security_group_id = aws_security_group.bastion-sg.id
  security_group_id = aws_security_group.jenkins-sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "docker_https" {
  from_port = local.docker_https
  protocol = local.tcp_protocol
  to_port = local.docker_https
  cidr_blocks = var.mgt_private_subnets
  security_group_id = aws_security_group.jenkins-sg.id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_alb" {
  from_port = local.http_port
  protocol = local.tcp_protocol
  to_port = local.http_port
  security_group_id = aws_security_group.jenkins-sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "jenkins_katalon_bolt" {
  from_port = local.neo4j_bolt
  protocol = local.tcp_protocol
  to_port = local.neo4j_bolt
  security_group_id = aws_security_group.jenkins-sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "jenkins_ssh" {
  from_port = local.ssh
  protocol = local.tcp_protocol
  to_port = local.ssh
  security_group_id = aws_security_group.jenkins-sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.jenkins-sg.id
  type = "egress"
}




#create alb target group
resource "aws_lb_target_group" "jenkins_target_group" {
  name = "${var.stack_name}-${var.env}-jenkin"
  port = local.http_port
  protocol = "HTTP"
  vpc_id = module.mgt-vpc.vpc_id
  stickiness {
    type = "lb_cookie"
    cookie_duration = 1800
    enabled = true
  }
  health_check {
    path = "/login"
    protocol = "HTTP"
    matcher = "200"
    interval = 15
    timeout = 3
    healthy_threshold = 2
    unhealthy_threshold = 2
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"jenkins-alb-target")
  },
  var.tags,
  )
}

resource "aws_lb_listener_rule" "frontend_alb_listener" {
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.alb_priority_rule
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.jenkins_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.jenkins_name}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }

}

resource "aws_lb_target_group_attachment" "jenkins" {
  target_group_arn = aws_lb_target_group.jenkins_target_group.arn
  target_id = aws_instance.jenkins.id
  port      = local.http_port
}
//#create boostrap script to hook up the node to ecs cluster
//resource "aws_ssm_document" "ssm_neo4j_boostrap" {
//  name          = "setup-jenkins"
//  document_type = "Command"
//  document_format = "YAML"
//  content = <<DOC
//---
//schemaVersion: '2.2'
//description: State Manager Bootstrap Example
//parameters: {}
//mainSteps:
//- action: aws:runShellScript
//  name: configureServer
//  inputs:
//    runCommand:
//    - set -ex
//    - cd /tmp
//    - rm -rf icdc-devops || true
//    - yum -y install epel-release
//    - yum -y install wget git python-setuptools python-pip
//    - pip install ansible==2.7.0 boto boto3 botocore
//    - git clone https://github.com/CBIIT/icdc-devops
//    - cd icdc-devops && git checkout master
//    - cd icrp
//    - ansible-playbook jenkins.yml"
//DOC
//  tags = merge(
//  {
//    "Name" = format("%s-%s",var.stack_name,"ssm-document")
//  },
//  var.tags,
//  )
//}
//
//resource "aws_ssm_association" "jenkins" {
//  name = aws_ssm_document.ssm_neo4j_boostrap.name
//  targets {
//    key   = "tag:Name"
//    values = ["${var.stack_name}-jenkins"]
//  }
//}

data "aws_route53_zone" "zone" {
  name  = var.domain_name
}


resource "aws_route53_record" "records" {
  name = var.jenkins_name
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}