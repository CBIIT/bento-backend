variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "container_replicas" {
  description = "specify the number of container to run"
  type = number
}
variable "frontend_container_port" {
  description = "port on which the container listens"
  type = number
  default = 80
}
variable "frontend_target_group_arn" {
  description = "name of the frontend alb target group"
  type = string
}
variable "backend_container_port" {
  description = "port on which the container listens"
  type = number
  default = 8080
}
variable "backend_target_group_arn" {
  description = "name of the backend alb target group"
  type = string
}