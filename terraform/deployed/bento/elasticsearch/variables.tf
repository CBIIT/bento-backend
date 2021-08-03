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
variable "elasticsearch_instance_type" {
  description = "type of instance to be used to create the elasticsearch cluster"
  type = string
  default = "t3.medium.elasticsearch"
}
variable "remote_state_bucket_name" {
  description = "name of the remote bucket to store or pull terraform state data"
  type = string
}
variable "elasticsearch_version" {
  type = string
  description = "specify es version"
  default =
}
variable "create_es_service_role" {
  type = bool
  default = false
  description = "change this value to true if running this script for the first time"
}