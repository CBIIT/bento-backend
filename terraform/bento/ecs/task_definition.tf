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
<<<<<<< HEAD
            "image":"cbiitssrepo/bento-frontend:27",
=======
            "image":"cbiitssrepo/bento-frontend",
>>>>>>> b9b506ce97e3b5d8317edd028dc7d20d4467d569
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
        { 
            "name":"backend",
<<<<<<< HEAD
            "image":"cbiitssrepo/bento-backend:27",
=======
            "image":"cbiitssrepo/bento-backend",
>>>>>>> b9b506ce97e3b5d8317edd028dc7d20d4467d569
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
