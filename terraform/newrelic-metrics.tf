variable "newrelic_account_id" {
  type        = string
  description = "Newrelic Account ID"
  sensitive   = true
}
variable "newrelic_api_key" {
  type        = string
  description = "Newrelic API Key"
  sensitive   = true
}
variable "account_level" {
  type        = string
  description = "whether the account is prod or non-prod"
}
variable "program" {
  type        = string
  description = "the program name"
  default = "crdc"
}
variable "create_newrelic_pipeline" {
  type        = bool
  description = "whether to create the newrelic pipeline"
  default = false
}
variable "newrelic_s3_bucket" {
  type        = string
  description = "the bucket to use for failed metrics"
}

module "new_relic_metric_pipeline" {
  count = var.create_newrelic_pipeline ? 1: 0
  source = "github.com/CBIIT/datacommons-devops/terraform/modules/firehose-metrics/"

  account_id                = data.aws_caller_identity.current.account_id
  app                       = var.stack_name
  http_endpoint_access_key  = var.newrelic_api_key
  level                     = var.account_level
  new_relic_account_id      = var.newrelic_account_id
  permission_boundary_arn   = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:policy/PermissionBoundary_PowerUser"
  program                   = var.program
  s3_bucket_arn             = var.newrelic_s3_bucket
}