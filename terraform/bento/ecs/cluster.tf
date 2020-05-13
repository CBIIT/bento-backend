resource "aws_ecs_cluster" "ecs-cluster" {
    name = var.ecs_cluster_name
}