variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}

variable "vpc_id" {
  description = "vpc id to to launch the ALB"
  type        = string
}

variable "region" {
  description = "aws region to use for this resource"
  type = string
  default = "us-east-1"
}

variable "enable_version" {
  description = "enable bucket versioning"
  default = false
  type = bool
}
variable "lifecycle_rule" {
  description = "object lifecycle rule"
  type = any
  default = []
}
variable "certificate_domain_name" {
  description = "domain name for the ssl cert"
  type = string
}

variable "cloud_platform" {
  description = "choose cloud platform to use. We have two - leidos or cloudone"
  default = "leidos"
  type = string
}
variable "internal_alb" {
  description = "is this alb internal?"
  default = false
  type = bool
}
variable "lb_type" {
  description = "Type of loadbalancer"
  type = string
  default = "application"
}
variable "aws_account_id" {
  type = map(string)
  description = "aws account to allow for alb s3 logging"
  default = {
    us-east-1 = "127311923021"
  }
}
variable "public_subnet_ids" {
  description = "Provide list of public subnets to use in this VPC. Example 10.0.1.0/24,10.0.2.0/24"
  type = list(string)
}

variable "private_subnet_ids" {
  description = "Provide list private subnets to use in this VPC. Example 10.0.10.0/24,10.0.11.0/24"
  type = list(string)
}
