locals {
  http_port    = 80
  any_port     = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  https_port   = "443"
  nih_ip_cidrs =  terraform.workspace == "prod" || terraform.workspace == "stage" || var.cloud_platform == "leidos" ? ["0.0.0.0/0"]: [ "129.43.0.0/16" , "137.187.0.0/16" , "10.128.0.0/9" , "165.112.0.0/16" , "156.40.0.0/16" , "10.208.0.0/21" , "128.231.0.0/16" , "130.14.0.0/16" , "157.98.0.0/16" , "10.133.0.0/16" ]
  all_ips      =  var.cloud_platform == "leidos" ? ["0.0.0.0/0"] : local.nih_ip_cidrs
  alb_subnet_ids = terraform.workspace == "prod" || terraform.workspace == "stage"  || var.cloud_platform == "leidos" ? var.public_subnet_ids : var.private_subnet_ids
  #app_url =  terraform.workspace == "prod" ? "${var.app_sub_domain}.${var.domain_name}" : "${var.app_sub_domain}-${terraform.workspace}.${var.domain_name}"
  allowed_alb_ip_range = terraform.workspace == "prod" || terraform.workspace == "stage" || var.cloud_platform == "leidos"  ?  local.all_ips : local.nih_ip_cidrs
  alb_log_bucket_name = var.cloud_platform == "leidos" ? "${var.stack_name}-alb-${terraform.workspace}-access-logs" : "${var.stack_name}-${var.cloud_platform}-alb-${terraform.workspace}-access-logs"
  acm_certificate_issued_type = var.cloud_platform == "leidos" ? "AMAZON_ISSUED" : "IMPORTED"
  ecs_task_role_name = "${var.stack_name}-${terraform.workspace}-ecs-task-role"
  ecs_task_execution_role_name = "${var.stack_name}-${terraform.workspace}-ecs-task-execution-role"
}