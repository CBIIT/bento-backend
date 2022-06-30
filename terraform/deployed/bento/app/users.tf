resource "aws_ecs_service" "users_service" {
  name              = "${var.stack_name}-${terraform.workspace}-users"
  cluster           = aws_ecs_cluster.ecs_cluster.id
  task_definition   = aws_ecs_task_definition.users.arn
  desired_count     = var.container_replicas
  iam_role          = aws_iam_role.ecs-service-role.name
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent = 100
  load_balancer {
    target_group_arn = aws_lb_target_group.users_target_group.arn
    container_name   = "users"
    container_port   = var.users_container_port
  }
  depends_on = [module.alb]
}

resource "aws_ecs_task_definition" "users" {
  family        = "${var.stack_name}-${terraform.workspace}-users"
  network_mode  = "bridge"
  cpu = "256"
  memory = "512"
  container_definitions = jsonencode(yamldecode(file("users.yml")))
  tags = merge(
  {
    "Name" =format("%s-%s-%s",var.stack_name,terraform.workspace,"task-definition")
  },
  var.tags,
  )
}


#create alb target group
resource "aws_lb_target_group" "users_target_group" {
  name = "${var.stack_name}-${terraform.workspace}-users"
  port = var.users_container_port
  protocol = "HTTP"
  vpc_id =  data.terraform_remote_state.network.outputs.vpc_id
  stickiness {
    type = "lb_cookie"
    cookie_duration = 1800
    enabled = true
  }
  health_check {
    path = "/api/users/ping"
    protocol = "HTTP"
    matcher = "200"
    interval = 15
    port = var.users_container_port
    timeout = 3
    healthy_threshold = 2
    unhealthy_threshold = 2
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"users-alb-target-group")
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_users_alb" {
  from_port = var.users_container_port
  protocol = local.tcp_protocol
  to_port = var.users_container_port
  security_group_id = aws_security_group.frontend_sg.id
  source_security_group_id = module.alb.alb_security_group_id
  type = "ingress"
}

resource "aws_lb_listener_rule" "users_alb_listener_prod" {
  count =  terraform.workspace ==  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.users_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.users_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/users/*"]
    }
  }
}

resource "aws_lb_listener_rule" "users_alb_listener" {
  count =  terraform.workspace !=  "prod" ? 1:0
  listener_arn = module.alb.alb_https_listener_arn
  priority = var.users_rule_priority
  action {
    type = "forward"
    target_group_arn = aws_lb_target_group.users_target_group.arn
  }

  condition {
    host_header {
      values = ["${lower(var.stack_name)}-${var.env}.${var.domain_name}"]
    }
  }
  condition {
    path_pattern  {
      values = ["/api/users/*"]
    }
  }

}
