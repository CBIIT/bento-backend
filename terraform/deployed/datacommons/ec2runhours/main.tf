module "lambda" {
  for_each = var.functions
  source = "../lambda"
  cloudwatch_event_rule = each.value.cloudwatch_event_rule
  cloudwatch_event_rule_description = each.value.cloudwatch_event_rule_description
  cloudwatch_event_rule_name = each.value.cloudwatch_event_rule_name
  lambda_function_name = each.value.function_name
  lambda_function_package_name = "${path.module}/${each.value.function_package_name}"
  region = var.region
  stack_name = var.stack_name
  tags = var.tags
}