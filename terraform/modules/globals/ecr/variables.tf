variable "repo_name" {
  type  = string
  description = "Name of the repository."
}

variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "tags" {
  description = "tags for the vpc"
  type = map(string)
  default = {}
}