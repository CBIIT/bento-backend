
resource "aws_launch_configuration" "asg_launch_config" {
  name              = "${var.stack_name}-${var.env}-launch-configuration"
  image_id          =  data.aws_ami.centos.id
  instance_type     =  var.fronted_instance_type
  iam_instance_profile = aws_iam_instance_profile.ecs-instance-profile.id
  security_groups   = [aws_security_group.frontend_sg.id]
  associate_public_ip_address = var.associate_public_ip_address
  key_name    = var.ssh_key_name
  user_data   = data.template_cloudinit_config.user_data.rendered
  root_block_device {
    volume_type   = var.evs_volume_type
    volume_size   = var.instance_volume_size
    delete_on_termination = true
  }

  lifecycle {
    create_before_destroy = true
  }

}

resource "aws_autoscaling_group" "asg_frontend" {
  name                 = join("-",[var.stack_name,var.env,var.frontend_asg_name])
  max_size = var.max_size
  min_size = var.min_size
  desired_capacity     = var.desired_ec2_instance_capacity
  vpc_zone_identifier  = data.terraform_remote_state.network.outputs.private_subnets_ids
  launch_configuration = aws_launch_configuration.asg_launch_config.name
  target_group_arns    = [aws_lb_target_group.frontend_target_group.arn,aws_lb_target_group.backend_target_group.arn]
  health_check_type    =  var.health_check_type
  tag {
    key = "Name"
    propagate_at_launch = true
    value = "${var.stack_name}-${var.env}-${var.frontend_asg_name}"
  }
  dynamic "tag" {
    for_each = var.tags
    content {
      key = tag.key
      value = tag.value
      propagate_at_launch = true
    }
  }
}

resource "aws_autoscaling_schedule" "shutdown" {
  autoscaling_group_name = aws_autoscaling_group.asg_frontend.name
  scheduled_action_name  = "bento-auto-stop"
  recurrence             = var.shutdown_schedule
  desired_capacity       = 0
}

resource "aws_autoscaling_schedule" "startup" {
  autoscaling_group_name = aws_autoscaling_group.asg_frontend.name
  scheduled_action_name  = "bento-auto-start"
  recurrence             = var.startup_schedule
  desired_capacity       = var.desired_ec2_instance_capacity
  min_size               = var.min_size
  max_size               = var.max_size
}

resource "aws_security_group" "frontend_sg" {
  name = "${var.stack_name}-${var.env}-frontend-sg"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s-frontend-sg",var.stack_name,var.env),
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_bastion_frontend" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_frontend_alb" {
  from_port = var.frontend_container_port
  protocol = local.tcp_protocol
  to_port = var.frontend_container_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "inbound_backend_alb" {
  from_port = var.backend_container_port
  protocol = local.tcp_protocol
  to_port = var.backend_container_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound_frontend" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.frontend_sg.id
  type = "egress"
}

#create alb target group
resource "aws_lb_target_group" "frontend_target_group" {
  name = "${var.stack_name}-${var.env}-frontend"
  port = var.frontend_container_port
  protocol = "HTTP"
  vpc_id =  data.terraform_remote_state.network.outputs.vpc_id
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
    "Name" = format("%s-%s",var.stack_name,"frontend-alb-target-group")
  },
  var.tags,
  )
}

#create alb target group
resource "aws_lb_target_group" "backend_target_group" {
  name = "${var.stack_name}-${var.env}-backend"
  port = var.backend_container_port
  protocol = "HTTP"
  vpc_id =  data.terraform_remote_state.network.outputs.vpc_id
  stickiness {
    type = "lb_cookie"
    cookie_duration = 1800
    enabled = true
  }
  health_check {
    path = "/ping"
    protocol = "HTTP"
    matcher = "200"
    interval = 15
    timeout = 3
    healthy_threshold = 2
    unhealthy_threshold = 2
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"backend-alb-target")
  },
  var.tags,
  )
}


resource "aws_lb_listener_rule" "frontend_alb_listener_prod" {
  count =  var.stack_name == "bento" && var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.fronted_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
  }

  condition {
    host_header {
      values = [var.domain_name]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }
}


resource "aws_lb_listener_rule" "frontend_alb_listener_prod_others" {
  count =   var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.fronted_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }
}


resource "aws_lb_listener_rule" "backend_alb_listener_prod" {
  count =  var.stack_name == "bento" && var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.backend_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.backend_target_group.arn
  }

  condition {
    host_header {
      values = [var.domain_name]
    }
  }
  condition {
    path_pattern  {
      values = ["/v1/graphql/*"]
    }
  }
}


resource "aws_lb_listener_rule" "backend_alb_listener_prod_others" {
  count =  var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.backend_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.backend_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/v1/graphql/*"]
    }
  }
}


resource "aws_lb_listener_rule" "frontend_alb_listener" {
  count =  var.stack_name == "bento" && var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.fronted_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }

}

resource "aws_lb_listener_rule" "frontend_alb_listener_others" {
  count =  var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.fronted_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }

}



resource "aws_lb_listener_rule" "backend_alb_listener" {
  count =  var.stack_name == "bento" && var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.backend_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.backend_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.env}.${var.domain_name}"]
    }

  }
  condition {
    path_pattern  {
      values = ["/v1/graphql/*"]
    }
  }
}

resource "aws_lb_listener_rule" "backend_alb_listener_others" {
  count =  var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.backend_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.backend_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}-${var.env}.${var.domain_name}"]
    }

  }
  condition {
    path_pattern  {
      values = ["/v1/graphql/*"]
    }
  }
}

resource "aws_lb_listener_rule" "www" {
  count =  var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = "120"
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
  }

  condition {
    host_header {
      values = [join(".",["www",var.domain_name])]
    }
  }
  condition {
    path_pattern  {
      values = ["/*"]
    }
  }
}

#create boostrap script to hook up the node to ecs cluster
resource "aws_ssm_document" "ssm_doc_boostrap" {
  name          = "${var.stack_name}-${var.env}-bootstrap-ecs-node"
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
    - ansible-playbook ecs-agent.yml --skip-tags master -e stack_name="${var.stack_name}" -e ecs_cluster_name="${var.ecs_cluster_name}-${var.env}" -e env="${var.env}"
    - systemctl restart docker
DOC
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ssm-document")
  },
  var.tags,
  )
}
#install monitoring agents
//resource "aws_ssm_document" "bento_doc" {
//  name          = "${var.env}-bootstrap-agents"
//  document_type = "Command"
//  document_format = "YAML"
//  content = <<DOC
//---
//schemaVersion: '2.2'
//description: State Manager Bootstrap Example
//parameters: {}
//mainSteps:
//- action: aws:runShellScript
//  name: configureAgents
//  inputs:
//    runCommand:
//    - set -ex
//    - cd /tmp/icdc-devops/icrp
//    - ansible-playbook agents.yml -e env="${var.env}" -e app=ecs -e platform="${var.platform}"
//  DOC
//  tags = merge(
//  {
//    "Name" = format("%s-%s",var.stack_name,"bento-install-agents")
//  },
//  var.tags,
//  )
//}

resource "aws_ssm_document" "bootstrap" {
  document_format = "YAML"
  document_type = "Command"
  name = "boostrap-${var.stack_name}-${var.env}-ecs-nodes"
  content = <<DOC
---
schemaVersion: '2.2'
description: Bootstrap ecs instances
parameters: {}
mainSteps:
- action: aws:runDocument
  name: configureECSAgent
  inputs:
    documentType: SSMDocument
    documentPath: ${var.env}-bootstrap-ecs
    documentParameters: "{}"


DOC
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"bootstrap-ecs-nodes")
  },
  var.tags,
  )
}

resource "aws_ssm_association" "bootstrap" {
  name = aws_ssm_document.ssm_doc_boostrap.name
  targets {
    key   = "tag:aws:autoscaling:groupName"
    values = [aws_autoscaling_group.asg_frontend.name]
  }
}