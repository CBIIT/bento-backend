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
variable "profile" {
  description = "iam user profile to use"
  type = string
}
variable "vpc_cidr_block" {
  description = "CIDR Block for this  VPC. Example 10.0.0.0/16"
  default = "10.10.0.0/16"
  type = string
}
variable "public_subnets" {
  description = "Provide list of public subnets to use in this VPC. Example 10.0.1.0/24,10.0.2.0/24"
  default     = []
  type = list(string)
}
variable "private_subnets" {
  description = "Provide list private subnets to use in this VPC. Example 10.0.10.0/24,10.0.11.0/24"
  default     = []
  type = list(string)
}
variable "availability_zones" {
  description = "list of availability zones to use"
  type = list(string)
  default = []
}
variable "env" {
  description = "environment"
  type = string
}