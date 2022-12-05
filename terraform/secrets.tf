#secrets
variable "create_shared_secrets" {
  type        = bool
  default     = false
  description = "shared secrets are created in this tier"
}

variable "neo4j_password" {
  type        = string
  description = "neo4j password"
}
variable "neo4j_ip" {
  type        = string
  description = "name of the db instance for this tier"
}
variable "indexd_url" {
  type        = string
  description = "indexd url"
}

variable "github_token" {
  type        = string
  description = "github token"
  sensitive   = true
}

module "secrets" {
  source                        = "git::https://github.com/CBIIT/datacommons-devops.git//terraform/modules/secrets?ref=terraform_modules"
  create_shared_secrets         = var.create_shared_secrets
  app                           = var.stack_name
  es_host                       = var.create_opensearch_cluster ? module.opensearch[0].opensearch_endpoint : ""
  neo4j_password                = var.neo4j_password
  neo4j_ip                      = var.neo4j_ip
  indexd_url                    = var.indexd_url
  github_token                  = var.github_token
  sumo_collector_token_frontend = module.monitoring.sumo_source_urls.frontend[0]
  sumo_collector_token_backend  = module.monitoring.sumo_source_urls.backend[0]
  sumo_collector_token_files    = module.monitoring.sumo_source_urls.files[0]
}