variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "stack_name" {
  description = "name of the project"
  type = string
}

variable "env" {
  type        = string
  description = "environment"
}

variable "db_subnet_ids" {
  type        = list(string)
  default     = []
  description = "list of subnet IDs to usee"
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

variable "snapshot_identifier_prefix" {
  type        = string
  default     = "bento"
  description = "final snapshot"
}

variable "skip_final_snapshot" {
  description = "specify snapshot if snapshot should be created on cluster destroy."
  type        = bool
  default     = false
}

variable "deletion_protection" {
  description = "prevent deletion"
  type        = bool
  default     = false
}

variable "backup_retention_period" {
  description = "number of days to keep backup"
  type        = number
  default     = 35
}

variable "backup_window" {
  description = "ideal time to perform backups."
  type        = string
  default     = "04:00-05:00"
}

variable "maintenance_window" {
  description = "time to perform maintenance"
  type        = string
  default     = "sun:00:00-sun:02:00"
}

variable "minor_version_upgrade" {
  description = "allow minor version upgrade"
  type        = bool
  default     = true
}

variable "db_engine_type" {
  description = "Aurora database engine type"
  type        = string
  default     = "aurora-mysql"
}

variable "db_engine_version" {
  description = "aurora database engine version."
  type        = string
  default     = "5.6.10a"
}

variable "db_engine_mode" {
  type        = string
  default     = "serverless"
  description = "The database engine mode."
}

variable "enabled_cloudwatch_logs_exports" {
  description = "List of log types to export to cloudwatch."
  type        = list(string)
  default     = ["audit", "error", "general", "slowquery", "postgresql"]
}

variable "availability_zones" {
  description = "list of availability zones"
  type        = list(any)
  default     = []
}
variable "enable_http_endpoint" {
  description = "enable HTTP endpoint for serverless."
  type        = bool
  default     = false
}
variable "min_capacity" {
  description = "The minimum capacity."
  type        = number
  default     = 1
}
variable "storage_encrypted" {
  description = "Enable underlying storage encryption."
  type        = bool
  default     = true
}
variable "allow_major_version_upgrade" {
  description = "Enable to allow major engine version upgrades when changing engine versions"
  type        = bool
  default     = false
}
variable "max_capacity" {
  description = "The maximum capacity."
  type        = number
  default     = 2
}
variable "master_password_length" {
  description = "length of master user password"
  type = number
  default = 15
}
variable "vpc_id" {
  type        = string
  description = "VPC ID the DB instance will be created in"
}

variable "secret_recovery_window_in_days" {
  description = "number of days to keep secret after deletion"
  type = number
  default = 0
}
variable "allowed_ip_blocks" {
  description = "allowed ip block for the rds ingress"
  type = list(string)
  default = []
}