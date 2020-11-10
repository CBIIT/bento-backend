variable "alb_name" {
  description = "Name for the ALB"
  type = string
  default = "alb"
}
variable "create_alb" {
  description = "choose to create alb or not"
  type = bool
  default = true
}
variable "lb_type" {
  description = "Type of loadbalance"
  type = string
  default = "application"
}
variable "internal_alb" {
  description = "is this alb internal?"
  default = false
  type = bool
}
variable "subnets" {
  description = "subnets to associate with this ALB"
  type = list(string)
}
variable "tags" {
  description = "tags to label this ALB"
  type = map(string)
  default = {}
}
variable "stack_name" {
  description = "Name of the project"
  type = string
}
variable "ssl_policy" {
  description = "specify ssl policy to use"
  default = "ELBSecurityPolicy-2016-08"
  type = string
}
variable "default_message" {
  description = "default message response from alb when resource is not available"
  default = "The request resource is not available"
}
variable "certificate_arn" {
  description = "certificate arn to use for the https listner"
  type = string
}
variable "vpc_id" {
  description = "VPC Id to to launch the ALB"
  type = string
}
variable "env" {
  description = "environment"
  type = string
}