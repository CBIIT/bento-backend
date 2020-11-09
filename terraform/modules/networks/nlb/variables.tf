variable "nlb_name" {
  description = "Name for the ALB"
  type = string
  default = "nlb"
}
variable "create_alb" {
  description = "choose to create alb or not"
  type = bool
  default = true
}
variable "lb_type" {
  description = "Type of loadbalance"
  type = string
  default = "network"
}
variable "internal_alb" {
  description = "is this alb internal?"
  default = true
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

variable "vpc_id" {
  description = "VPC Id to to launch the ALB"
  type = string
}
variable "nlb_listener_port" {
  description = "load balance port to listen traffic on "
  type = number
}
variable "default_message" {
  description = "default message on nlb listerner"
  default = "This is a NLB listener underconstruction"
  type = string
}
variable "deregistration_delay" {
  description = "number of seconds it takes to stop sending traffic to instance taken out of service"
  default = 90
  type = number
}
variable "health_check_interval" {
  description = "how often to check livelininess of the instances"
  default = "30"
}