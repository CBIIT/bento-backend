locals {
  http_port    = 80
  any_port     = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  https_port   = "443"
  bastion_port = 22
  neo4j_http = 7474
  neo4j_https = 7473
  neo4j_bolt = 7687
  redis = "6379"
  integration_server_profile_name = "${var.iam_prefix}-integration-server-profile"
  permissions_boundary            = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:policy/PermissionBoundary_PowerUser"
  cert_types = var.cloud_platform == "leidos" ? "AMAZON_ISSUED" : "IMPORTED"
  #nih_ip_cidrs =  terraform.workspace == "prod" || terraform.workspace == "stage" || var.cloud_platform == "leidos" ? ["0.0.0.0/0"]: [ "129.43.0.0/16" , "137.187.0.0/16"  , "165.112.0.0/16" , "156.40.0.0/16"  , "128.231.0.0/16" , "130.14.0.0/16" , "157.98.0.0/16"]
  nih_ip_cidrs  = ["0.0.0.0/0"]
  all_ips      =  var.cloud_platform == "leidos" ? ["0.0.0.0/0"] : local.nih_ip_cidrs
  alb_subnet_ids = terraform.workspace == "prod" || terraform.workspace == "stage"  || var.cloud_platform == "leidos" ? var.public_subnet_ids : var.private_subnet_ids
  #app_url =  terraform.workspace == "prod" ? "${var.app_sub_domain}.${var.domain_name}" : "${var.app_sub_domain}-${terraform.workspace}.${var.domain_name}"
  allowed_alb_ip_range = terraform.workspace == "prod" || terraform.workspace == "stage" || var.cloud_platform == "leidos"  ?  local.all_ips : local.nih_ip_cidrs
  alb_log_bucket_name = var.cloud_platform == "leidos" ? "alb-access-logs" : "${var.cloud_platform}-alb-access-logs"
  acm_certificate_issued_type = var.cloud_platform == "leidos" ? "AMAZON_ISSUED" : "IMPORTED"
  ecs_task_role_name = "${var.stack_name}-${terraform.workspace}-ecs-task-role"
  ecs_task_execution_role_name = "${var.stack_name}-${terraform.workspace}-ecs-task-execution-role"
  application_url =  terraform.workspace == "prod" ? var.domain_name : "${var.application_subdomain}-${terraform.workspace}.${var.domain_name}"
  fargate_security_group_ports = var.cloud_platform == "leidos" ? ["80","443","3306","7473","7474","7687"] : ["443","3306","7473","7474","7687"]
  managed_policy_arns = [
    "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore",
    "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy",
    "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess"
  ]
}
