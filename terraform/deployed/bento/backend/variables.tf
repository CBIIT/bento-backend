
variable "kms_key_id" {
  default = ""
  description = "The AWS KMS master key ID used for the SSE-KMS encryption on the tf state s3 bucket. It is defaulted to aws/s3"
}
variable "tags" {
  description = "tags to associate to the resources"
  type = map(string)
}
variable "stack_name" {
  description = "project name"
  type = string
}

variable "region" {
  description = "aws region to use"
  type = string
}
variable "profile" {
  description = "iam profile use to deploy resources"
  type = string
}
