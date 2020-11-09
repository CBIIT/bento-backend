variable index_document {
  description = "name of the home page"
  default = "index.html"
}
variable "error_document" {
  description = "name of the error document"
  default = "access_log"
}
variable app {
  description = "name of the static website"
  default = ""
  type = string
}
variable "tags" {
  description = "tags for the vpc"
  type = map(string)
  default = {}
}
variable "domain" {
  description = "domain name for the website"
}
variable "stack_name" {
  description = "name of the project"
  type = string
}