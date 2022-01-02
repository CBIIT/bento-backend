variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
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
variable "alb_s3_bucket_name" {
  type = string
  description = "name of s3 bucket for alb logging"
  default     = null
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
variable "alb_name" {
  type = string
  description = "name of alb"
}