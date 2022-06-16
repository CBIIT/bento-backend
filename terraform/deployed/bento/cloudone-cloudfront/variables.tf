variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "cloudfront_distribution_bucket_name" {
  description = "specify the name of s3 bucket for cloudfront"
  type = string
}
variable "cloudfront_distribution_log_bucket_name" {
  description = "specify the name of s3 bucket for the cloudfront logs"
  type = string
}
variable "cloudfront_origin_acess_identity_description" {
  description = "description for OAI"
  type = string
  default = "cloudfront origin access identify for s3"
}
variable "cloudfront_log_path_prefix_key" {
  description = "path prefix to where cloudfront send logs to s3 bucket"
  type = string
  default = "cloudfront/logs"
}