resource "aws_iam_role" "lambda_role" {
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
  name = "${var.stack_name}-${terraform.workspace}-lambda-role"
  tags = var.tags
}

resource "aws_iam_policy" "lambda_iam_policy" {
  policy = data.aws_iam_policy_document.lambda_exec_role_policy.json
  name = "${var.stack_name}-${terraform.workspace}-lambda-exec-policy"

}

resource "aws_iam_policy_attachment" "lambda_exec_policy_attachment" {
  name = "${var.stack_name}-${terraform.workspace}-lambda-exec-policy-attachement"
  policy_arn = aws_iam_policy.lambda_iam_policy.arn
  roles = [aws_iam_role.lambda_role.name]
}

resource "aws_lambda_function" "lambda" {
  for_each = var.functions
  filename = each.value.function_package_name
  function_name = "${var.stack_name}-${terraform.workspace}-${each.value.function_name}"
  role = aws_iam_role.lambda_role.arn
  handler = "${each.value.function_name}.handler"
  memory_size = 512
  timeout = 60
  source_code_hash = filebase64sha256(each.value.function_package_name)
  runtime = "python3.8"
  tags = var.tags
}

resource "aws_cloudwatch_event_rule" "event_rule" {
  for_each = var.functions
  name = "${var.stack_name}-${terraform.workspace}-${each.value.cloudwatch_event_rule_name}"
  description = each.value.cloudwatch_event_rule_description
  schedule_expression = each.value.cloudwatch_event_rule
  tags = var.tags
}

resource "aws_cloudwatch_event_target" "rule_target" {
  for_each = var.functions
  rule = aws_cloudwatch_event_rule.event_rule[each.key].name
  target_id = "${var.stack_name}-${terraform.workspace}-${each.value.function_name}"
  arn = aws_lambda_function.lambda[each.key].arn
}

resource "aws_lambda_permission" "cloudwatch_invoke_lambda" {
  for_each = var.functions
  statement_id = "AllowInvocation${each.value.function_name}"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda[each.key].function_name
  principal = "events.amazonaws.com"
  source_arn = aws_cloudwatch_event_rule.event_rule[each.key].arn
}