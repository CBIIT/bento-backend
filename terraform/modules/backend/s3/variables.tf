variable "remote_state_bucket" {
  description = "name of the remote state bucket"
  type = string
}
variable "remote_state_key" {
  description = "name of the path to the terraform state"
}