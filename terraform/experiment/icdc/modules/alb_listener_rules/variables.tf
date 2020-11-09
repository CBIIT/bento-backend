variable "vpc_id" {}


variable "health_check" {
  type = "map"
  default = {}
}

# variable "target_group_name" {}
variable "target_group_sticky" {
  default = true
}

# variable "alb_listener_port" {}
# variable "alb_listener_protocol" {}
variable "alb_arn" {}
variable "domain" {
  default = "jilivay.com"
}
variable "services_map" {
  type = "map"
  default = {}
}
# variable "instance_id" 
# { 
#   default = [], 
#   type = "list" 
# }
variable "tag_name" {}
 variable "create_listener" {
   default = false
 }
variable "forward_protocol" {
  default = {}
}
variable "listener_port" {
  default = {}
}

