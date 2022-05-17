
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "region" {
  description = "aws region to deploy"
  type = string
}
variable "lambda_function_package_name" {
  description = "name of the lambda function"
  type = string
}
variable "lambda_function_name" {
  description = "name of the lambda function"
  type = string
}
variable "cloudwatch_event_rule_name" {
  description = "cloudwatch event rule name"
  type = string
}
variable "cloudwatch_event_rule" {
  description = "cloudwatch event rule"
  type = string
}
variable "cloudwatch_event_rule_description" {
  description = "cloudwatch event rule description"
  type = string
}