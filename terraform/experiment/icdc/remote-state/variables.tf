variable "profile" {
  description = "Profile for launching vm"
  default = "default"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}

variable "terraform-s3-bucket-name" {
  default   = "evay-terraform-state"
  description = "name of the s3 bucket to store terraform remote state"
}

variable "environment" {
  description  = "name of the environment, example dev,qa,etc"
  default = "dev"
}
variable "kms_key_id" {
  default = ""
  description = "The AWS KMS master key ID used for the SSE-KMS encryption. This default to aws/s3 default"
}