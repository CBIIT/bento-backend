variable "profile" {
  description = "Profile for launching vm"
  default = "icdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}
variable "s3_bucket_name" {
  default = "icdc-dev-alb-logs"
}
variable "alb_name" {
  default = "icdc-dev-k9dc-alb"
}
