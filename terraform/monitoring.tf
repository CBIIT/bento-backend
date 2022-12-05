#monitoring
variable "sumologic_access_id" {
  type        = string
  description = "Sumo Logic Access ID"
}
variable "sumologic_access_key" {
  type        = string
  description = "Sumo Logic Access Key"
  sensitive   = true
}

#variable "microservices" {
#  type = map(object({
#    name                      = string
#    port                      = number
#    health_check_path         = string
#    priority_rule_number      = number
#    image_url                 = string
#    cpu                       = number
#    memory                    = number
#    path                      = list(string)
#    number_container_replicas = number
#  }))
#}

module "monitoring" {
  source = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/monitoring?ref=terraform_modules"
  app                  = var.stack_name
  tags                 = var.tags
  sumologic_access_id  = var.sumologic_access_id
  sumologic_access_key = var.sumologic_access_key
  microservices        = var.microservices
}