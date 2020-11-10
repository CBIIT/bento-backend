variable "tags" {
  description = "tags to label this ALB"
  type = map(string)
  default = {}
}
variable "stack_name" {
  description = "Name of the project"
  type = string
}
variable "target_arns" {
  description = "specify target group arns"
  type = list(string)
}
variable "endpoint_configuration" {
  description = "specify the types of api endpoint configuration "
  type = list(string)
  default = ["EDGE"]
}
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

variable "nlb_dns_name" {
  description = "alb dns name"
}