variable "vpc_id" {
  type        = string
  description = "VPC ID the DB instance will be created in"
}
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}

variable "region" {
  description = "aws region to deploy"
  type = string
  default = "us-east-1"
}

variable "db_subnet_ids" {
  type        = list(string)
  default     = []
  description = "list of subnet IDs to usee"
}
variable "allowed_ip_blocks" {
  description = "allowed ip block for the rds ingress"
  type = list(string)
  default = []
}
variable "db_engine_mode" {
  type        = string
  default     = "serverless"
  description = "The database engine mode."
}
variable "db_engine_version" {
  description = "aurora database engine version."
  type        = string
  default     = "5.6.10a"
}
variable "db_engine_type" {
  description = "Aurora database engine type"
  type        = string
  default     = "aurora-mysql"
}
variable "db_instance_class" {
  description = "Instance type to use for the db"
  type        = string
  default     = "db.serverless"
}
variable "master_username" {
  description = "username for this db"
  type        = string
  default     = ""
  sensitive   = true
}
variable "database_name" {
  description = "name of the database"
  type = string
  default = "bento"
}