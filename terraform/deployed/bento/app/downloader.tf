resource "aws_ecs_service" "downloader_service" {
  name              = "${var.stack_name}-${var.env}-file-downloader"
  cluster           = aws_ecs_cluster.ecs_cluster.id
  task_definition   = aws_ecs_task_definition.downloader.arn
  desired_count     = var.container_replicas
  iam_role          = aws_iam_role.ecs-service-role.name
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent = 100
  load_balancer {
    target_group_arn = aws_lb_target_group.downloader_target_group.arn
    container_name   = "downloader"
    container_port   = var.downloader_container_port
  }
  depends_on = [module.alb]
}

resource "aws_ecs_task_definition" "downloader" {
  family        = "${var.stack_name}-${var.env}-file-downloader"
  network_mode  = "bridge"
  cpu = "512"
  memory = "1024"
  container_definitions = jsonencode(yamldecode(file("downloader.yml")))
  tags = merge(
  {
    "Name" =format("%s-%s-%s",var.stack_name,var.env,"task-definition")
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_downloader_alb" {
  from_port = var.downloader_container_port
  protocol = local.tcp_protocol
  to_port = var.downloader_container_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

#create alb target group
resource "aws_lb_target_group" "downloader_target_group" {
  name = "${var.stack_name}-${var.env}-downloader"
  port = var.downloader_container_port
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
    "Name" = format("%s-%s",var.stack_name,"downloader-alb-target-group")
  },
  var.tags,
  )
}

resource "aws_lb_listener_rule" "downloader_alb_listener_prod" {
  count =  var.stack_name == "bento" && var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.downloader_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.downloader_target_group.arn
  }

  condition {
    host_header {
      values = [var.domain_name]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/files/*"]
    }
  }
}

resource "aws_lb_listener_rule" "downloader_alb_listener_prod_others" {
  count =  var.env ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.downloader_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.downloader_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/files/*"]
    }
  }
}


resource "aws_lb_listener_rule" "downloader_alb_listener" {
  count =  var.stack_name == "bento" && var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.downloader_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.downloader_target_group.arn
  }

  condition {
    host_header {
      values = ["${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/files/*"]
    }
  }

}


resource "aws_lb_listener_rule" "downloader_alb_listener_others" {
  count =  var.stack_name != "bento" && var.env !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.downloader_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.downloader_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/files/*"]
    }
  }

}