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
  filename = var.lambda_function_package_name
  function_name = "${var.stack_name}-${terraform.workspace}-${var.lambda_function_name}"
  role = aws_iam_role.lambda_role.arn
  handler = "${var.lambda_function_name}.handler"
  memory_size = 512
  timeout = 60
  source_code_hash = filebase64sha256(var.lambda_function_package_name)
  runtime = "python3.8"
  tags = var.tags
}

resource "aws_cloudwatch_event_rule" "event_rule" {
  name = "${var.stack_name}-${terraform.workspace}-${var.cloudwatch_event_rule_name}"
  description = var.cloudwatch_event_rule_description
  schedule_expression = var.cloudwatch_event_rule
  tags = var.tags
}

resource "aws_cloudwatch_event_target" "rule_target" {
  rule = aws_cloudwatch_event_rule.event_rule.name
  target_id = "${var.stack_name}-${terraform.workspace}-${var.lambda_function_name}"
  arn = aws_lambda_function.lambda.arn
}

resource "aws_lambda_permission" "cloudwatch_invoke_lambda" {
  statement_id = "AllowInvocation${var.lambda_function_name}"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda.function_name
  principal = "events.amazonaws.com"
  source_arn = aws_cloudwatch_event_rule.event_rule.arn
}