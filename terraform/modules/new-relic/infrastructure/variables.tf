variable "alert_policy_name" {
  default = ""
}
variable "incident_preference" {
  default     = ""
  type        = string
}
variable "email_channel" {
  type        = string
  default     = null
}
variable "recipients" {
  type        = string
  default     = null
}
variable "slack_channel" {
  type        = string
  default     = null
}
variable "slack_channel_name" {
  type        = string
  default     = null
}
variable "slack_url" {
  type        = string
  default     = null
}
variable "host_condition" {
  type        = string
  default     = null
}
