variable "region" {
  description = "aws region to use"
  type = string
}
variable "profile" {
  description = "user profile to use"
  type = string
}
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "management_environment" {
  default = "management"
  description = "management environment"
}
variable "env" {
  description = "specify environment to use in vpc peering"
  type = string
}