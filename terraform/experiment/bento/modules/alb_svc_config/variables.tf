variable "stack" {
}

variable "vpc_id" {
}

variable "alb_arn" {
}

variable "alb_listener_port" {
  default = 443
}

variable "svc_name" {
}

variable "target_port" {
  default = 80
}

variable "target_proto" {
  default = "HTTP"
}

variable "domain_name" {
}

variable "target_group_sticky" {
  default = true
}

variable "health_check" {
  type    = map(string)
  default = {}
}

variable "priority" {
  default = ""
}

variable "listener_arn" {
}

