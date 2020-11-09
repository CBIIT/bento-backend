variable "domain_name" {
  default = "essential-dev.com"
}
variable "evaluate_target_health" {
  default = true
}
variable "dns_name" {}
variable "alb_zone_id" {}
variable "hostnames" {
  type = "list"
}
