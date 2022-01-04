variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
  default = {
    ManagedBy = "terraform"
    Project = "bento"
    Environment = "prod"
    POC = "Amit"
  }
}
variable "stack_name" {
  description = "name of the project"
  type = string
  default = "bento"
}
variable "region" {
  description = "aws region to deploy"
  type = string
  default = "us-east-1"
}
variable "profile" {
  description = "iam user profile to use"
  type = string
  default = "icdc"
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