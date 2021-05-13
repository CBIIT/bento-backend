
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
}
variable "profile" {
  description = "iam user profile to use"
  type = string
}

variable "domain_name" {
  description = "domain name for the application"
  type = string
}
variable "env" {
  description = "environment"
  type = string
}

variable "cloudfront_distribution_bucket_name" {
  description = "specify the name of s3 bucket for cloudfront"
  type = string
}



