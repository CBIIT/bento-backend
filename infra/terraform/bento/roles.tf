
module "ecs_task_role" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/roles"
  iam_role_name = local.ecs_task_role_name
  custom_policy_name = "${var.stack_name}-${terraform.workspace}-ecs-task-policy"
  iam_policy_description = "${var.stack_name}-${terraform.workspace} ecs task policy"
  add_custom_policy = true
  iam_policy = data.aws_iam_policy_document.ecs_task_policy.json
  trusted_role_services = [
      "ecs-tasks.amazonaws.com"
  ]
  tags = var.tags
}

module "ecs_task_execution_role" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/roles"
  iam_role_name = local.ecs_task_execution_role_name
  custom_policy_name = "${var.stack_name}-${terraform.workspace}-ecs-task-execution-policy"
  iam_policy_description = "${var.stack_name}-${terraform.workspace} ecs task execution policy"
  trusted_role_services = [
      "ecs-tasks.amazonaws.com"
  ]
  add_custom_policy = true
  custom_role_policy_arns = ["arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"]
  iam_policy  = data.aws_iam_policy_document.task_execution_policy_document.json
  tags = var.tags
}
