#create ecs cluster
# resource "aws_service_discovery_private_dns_namespace" "namespace" {
#   name        = "${var.stack_name}.bento"
#   description = "ecs dns namespace"
#   vpc         = data.terraform_remote_state.network.outputs.vpc_id
# }

# resource "aws_service_discovery_service" "frontend" {
#   name = "frontend"
#   dns_config {
#     namespace_id = aws_service_discovery_private_dns_namespace.namespace.id
#     dns_records {
#       ttl  = 10
#       type = "SRV"
#     }
#     routing_policy = "MULTIVALUE"
#   }
#   health_check_custom_config {
#     failure_threshold = 1
#   }
# }

# resource "aws_service_discovery_service" "backend" {
#   name = "backend"
#   dns_config {
#     namespace_id = aws_service_discovery_private_dns_namespace.namespace.id
#     dns_records {
#       ttl  = 10
#       type = "SRV"
      
#     }
#     routing_policy = "MULTIVALUE"
#   }
#   health_check_custom_config {
#     failure_threshold = 1
#   }
# }

resource "aws_ecs_service" "frontend" {
  name              = "ctdc_ecs_frontend"
  cluster           = aws_ecs_cluster.ecs-cluster.id
  task_definition   = aws_ecs_task_definition.frontend.arn
  desired_count     = 1
  iam_role          = aws_iam_role.ecs-service-role.name
  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent = 200
  
  load_balancer {
    target_group_arn = data.aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = 80
  }
  # service_registries {
  #   registry_arn = aws_service_discovery_service.frontend.arn
  #   container_name = "frontend"
  #   container_port = 80
  # }

}


resource "aws_ecs_service" "backend" {
  name              = "ctdc_ecs_backend"
  cluster           = aws_ecs_cluster.ecs-cluster.id
  task_definition   = aws_ecs_task_definition.backend.arn
  desired_count     = 1
  iam_role          = aws_iam_role.ecs-service-role.name
  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent = 200
  # service_registries {
  #   registry_arn = aws_service_discovery_service.backend.arn
  #   container_name = "backend"
  #   container_port = 8080
  # }
  load_balancer {
    target_group_arn = data.aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = 8080
  }

}