# data "aws_ecs_task_definition" "frontend" {
#   task_definition = aws_ecs_task_definition.ctdc.family
#   depends_on = [aws_ecs_task_definition.ctdc]
# }
resource "aws_ecs_task_definition" "frontend" {
  family         = "bento-frontend"
  network_mode   = "bridge"
  memory = "512"
  cpu = "512"
  container_definitions = <<DEFINITION
    [
        {
            "name":"frontend",
            "image":"cbiitssrepo/bento-frontend:35",
            "essential":true,
            "portMappings":[
                {
                "containerPort":80,
                "hostPort":80
                }
            ]
        }
    ]
DEFINITION
}

resource "aws_ecs_task_definition" "backend" {
  family         = "bento-backend"
  network_mode   = "bridge"
  memory = "512"
  cpu = "512"
  container_definitions = <<DEFINITION
    [
        {   "name":"backend",
            "image":"cbiitssrepo/bento-backend:35",
            "essential":true,
            "portMappings":[
                {
                "containerPort":8080,
                "hostPort":8080
                }
            ]
        }
    ]
DEFINITION
}