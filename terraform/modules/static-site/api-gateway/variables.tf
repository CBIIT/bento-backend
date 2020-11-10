variable "api_gateway_name" {
  description = "name of the api gateway"
  type = string
}
variable "s3_uri" {
  description = "specify s3 uri"
  type = string
}
variable "api_stage_name" {
  description = "specify the name of api deployment stage"
  type = string
}
variable "domain_name" {
  description = "specify the domain name to use for this resource"
  type = string
}
variable "certificate_arn" {
  description = "certificate arn to use"
  type = string
}
variable "tags" {
  description = "tags for the vpc"
  type = map(string)
  default = {}
}
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "region" {
  description = "aws region to use for resources"
  type = string
}
variable "alb_dns_name" {
  description = "alb dns name"
}