

locals {
  admin_password = "${secret_resource.admin_password.value}"
  neo4j_password = "${secret_resource.neo4j_password.value}" 
  sumologic_accessid = "${secret_resource.sumologic_accessid.value}"
  sumologic_accesskey = "${secret_resource.sumologic_accesskey.value}"
}

resource "aws_ssm_parameter" "admin_password" {
  name        = "admin_password"
  description = "The admin password"
  type        = "SecureString"
  value       = "${local.admin_password}"
  overwrite   = true
  tags = {
    environment = "${var.environment}"
  }
}
resource "aws_ssm_parameter" "neo4j_password" {
  name        = "neo4j_password"
  description = "Neo4j password"
  type        = "SecureString"
  value       = "${local.neo4j_password}"
  overwrite   = true
  tags = {
    environment = "${var.environment}"
  }
}
resource "aws_ssm_parameter" "sumologic_accessid" {
  name        = "sumologic_accessid"
  description = "Sumologic Collector accessid"
  type        = "SecureString"
  value       = "${local.sumologic_accessid}"
  overwrite   = true
  tags = {
    environment = "${var.environment}"
  }
}
resource "aws_ssm_parameter" "sumologic_accesskey" {
  name        = "sumologic_accesskey"
  description = "Sumologic Collector accesskey"
  type        = "SecureString"
  value       = "${local.sumologic_accesskey}"
  overwrite   = true
  tags = {
    environment = "${var.environment}"
  }
}