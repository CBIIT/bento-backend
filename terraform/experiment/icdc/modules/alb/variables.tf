variable "alb_name" {
}

variable "internal_alb" {
  default = false
}

variable "s3_bucket_name" {
}

variable "alb_subnets" {
  type = list(string)
}

variable "alb_security_groups" {
  type = list(string)
}

variable "idle_timeout" {
  default = 60
}

