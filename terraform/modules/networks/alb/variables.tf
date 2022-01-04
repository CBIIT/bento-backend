variable "alb_name" {
  description = "Name for the ALB"
  type        = string
  default     = "alb"
}
variable "create_alb" {
  description = "choose to create alb or not"
  type        = bool
  default     = true
}
variable "lb_type" {
  description = "Type of loadbalance"
  type        = string
  default     = "application"
}
variable "internal_alb" {
  description = "is this alb internal?"
  default     = false
  type        = bool
}
variable "subnets" {
  description = "subnets to associate with this ALB"
  type        = list(string)
}
variable "tags" {
  description = "tags to label this ALB"
  type        = map(string)
  default     = {}
}
variable "stack_name" {
  description = "Name of the project"
  type        = string
}
variable "ssl_policy" {
  description = "specify ssl policy to use"
  default     = "ELBSecurityPolicy-2016-08"
  type        = string
}
variable "default_message" {
  description = "default message response from alb when resource is not available"
  default     = "The request resource is not available"
}
variable "certificate_arn" {
  description = "certificate arn to use for the https listner"
  type        = string
}
variable "vpc_id" {
  description = "VPC Id to to launch the ALB"
  type        = string
}
variable "env" {
  description = "environment"
  type        = string
}

#added frontend app name to accomodate ppdc-otg and ppdc-otp
variable "frontend_app_name" {
  description = "it will be either otp or otg"
  type        = string
  default     = ""
}
variable "s3_object_expiration_days" {
  description = "number of days for object to live"
  type = number
  default = 720
}
variable "s3_object_nonactive_expiration_days" {
  description = "number of days to retain non active objects"
  type = number
  default = 90
}
variable "s3_object_standard_ia_transition_days" {
  description = "number of days for an object to transition to standard_ia storage class"
  default = 120
  type = number
}
variable "s3_object_glacier_transition_days" {
  description = "number of days for an object to transition to glacier storage class"
  default = 180
  type = number
}
variable "aws_account_id" {
  type = map(string)
  description = "aws account to allow for alb s3 logging"
  default = {
    us-east-1 = "127311923021"
  }
}
