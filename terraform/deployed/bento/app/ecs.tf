#create ecs cluster
resource "aws_ecs_cluster" "ecs_cluster" {
  name = "${var.ecs_cluster_name}-${var.env}"

  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ecs-cluster")
  },
  var.tags,
  )

}

resource "aws_ecs_service" "ecs_service" {
  name              = "${var.stack_name}-${var.env}-frontend"
  cluster           = aws_ecs_cluster.ecs_cluster.id
  task_definition   = aws_ecs_task_definition.frontend.arn
  desired_count     = var.container_replicas
  iam_role          = aws_iam_role.ecs-service-role.name
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent = 100
  load_balancer {
    target_group_arn = aws_lb_target_group.frontend_target_group.arn
    container_name   = "frontend"
    container_port   = var.frontend_container_port
  }
  depends_on = [module.alb]
}

 resource "aws_ecs_service" "ecs_service_backend" {
   name              = "${var.stack_name}-${var.env}-backend"
   cluster           = aws_ecs_cluster.ecs_cluster.id
   task_definition   = aws_ecs_task_definition.backend.arn
   desired_count     = var.container_replicas
   iam_role          = aws_iam_role.ecs-service-role.name
   deployment_minimum_healthy_percent = 0
   deployment_maximum_percent = 100
   load_balancer {
     target_group_arn = aws_lb_target_group.backend_target_group.arn
     container_name   = "backend"
     container_port   = var.backend_container_port
   }
   depends_on = [module.alb]
 }

resource "aws_ecs_task_definition" "frontend" {
  family        = "${var.stack_name}-${var.env}-frontend"
  network_mode  = "bridge"
  cpu = "512"
  memory = "1024"
  container_definitions = jsonencode(yamldecode(file("frontend.yml")))
  tags = merge(
  {
    "Name" =format("%s-%s-%s",var.stack_name,var.env,"task-definition")
  },
  var.tags,
  )
}

resource "aws_ecs_task_definition" "backend" {
  family        = "${var.stack_name}-${var.env}-backend"
  network_mode  = "bridge"
  cpu = "512"
  memory = "1024"
  container_definitions = jsonencode(yamldecode(file("backend.yml")))
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.env,"task-definition")
  },
  var.tags,
  )
}