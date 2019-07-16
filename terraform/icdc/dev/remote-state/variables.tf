variable "profile" {
  description = "Profile for launching vm"
  default = "icdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}

variable "terraform-s3-bucket-name" {
  default   = "icdc-sandbox-terraform-state"
  description = "name of the s3 bucket to store terraform remote state"
}

variable "environment" {
  description  = "name of the environment, example dev,qa,etc"
  default = "sandbox"
}
variable "kms_key_id" {
  default = ""
  description = "The AWS KMS master key ID used for the SSE-KMS encryption on the tf state s3 bucket. It is defaulted to aws/s3"
}