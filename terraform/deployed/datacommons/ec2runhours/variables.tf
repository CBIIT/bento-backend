
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
  default = "us-east-1"
}

variable "functions" {
  description = "functions to be deployed"
  type = map(object({
    function_name = string
    function_package_name = string
    cloudwatch_event_rule_description = string
    cloudwatch_event_rule_name = string
    cloudwatch_event_rule = string
  }))
}