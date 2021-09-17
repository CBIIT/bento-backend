variable "alert_policy_name" {
  default = ""
  type        = string
}
variable "incident_preference" {
  default     = ""
  type        = string
}
variable "email_channel" {
  type        = string
  default     = ""
}
variable "recipients" {
  type        = string
  default     = ""
}
variable "slack_channel" {
  type        = string
  default     = ""
}
variable "slack_channel_name" {
  type        = string
  default     = ""
}
variable "slack_url" {
  type        = string
  default     = ""
}
variable "host_condition" {
  type        = string
  default     = ""
}
variable "alert_policy_disk_utilization_name" {
  default = ""
}
variable "alert_policy_cpu_usage_name" {
  default = ""
}
variable "alert_policy_host_reporting_name" {
  default = ""
}
variable "host_condition_database" {
  default = ""
}
variable "stack_name" {
  default = ""
}
variable "frontend_app_name" {
  default = ""
}
variable "env" {
  default = ""
}