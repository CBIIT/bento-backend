variable "alb_name" {}
variable "internal_alb" {
  default = false
}
variable "s3_bucket_name" {}
variable "alb_subnets" {
  type = "list"
}
variable "alb_security_groups" {
  type = "list"
}
variable "idle_timeout" {
  default = 60
}






