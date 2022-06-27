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
}
#Description : Terraform label module variables.
variable "name" {
  type        = string
  description = "Name  (e.g. `app` or `cluster`)."
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

variable "replica_count" {
  description = "number of read replica count"
  type        = number
  default     = 1
}

variable "db_instance_type" {
  description = "Instance type to use for the db"
  type        = string
  default     = ""
}

variable "database_name" {
  description = "name of the database"
  type        = string
  default     = ""
}

variable "master_username" {
  description = "username for this db"
  type        = string
  default     = ""
  sensitive   = true
}

variable "master_password" {
  description = "password for the master username"
  type        = string
  default     = ""
  sensitive   = true
}

variable "snapshot_identifier_prefix" {
  type        = string
  default     = "bento"
  description = "Final snapshot"
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
  default     = 60
}

variable "backup_window" {
  description = "ideal time to perform backups."
  type        = string
  default     = "00:00-05:00"
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

variable "db_parameter_group_name" {
  type        = string
  default     = "default.aurora5.6"
  description = "The name of a DB parameter group to use."
  sensitive   = true
}

variable "db_cluster_parameter_group_name" {
  type        = string
  default     = "default.aurora5.6"
  description = "The name of a DB Cluster parameter group to use."
  sensitive   = true
}

variable "db_engine" {
  description = "Aurora database engine type"
  type        = string
  default     = "aurora-mysql"
}

variable "engine_version" {
  type        = string
  default     = "5.6.10a"
  description = "Aurora database engine version."
}

variable "engine_mode" {
  type        = string
  default     = "serverless"
  description = "The database engine mode."
}

variable "replica_scale_enabled" {
  type        = bool
  default     = false
  description = "Whether to enable autoscaling for RDS Aurora (MySQL) read replicas."
}

variable "replica_scale_max" {
  type        = number
  default     = 0
  description = "Maximum number of replicas to allow scaling."
}

variable "replica_scale_min" {
  type        = number
  default     = 2
  description = "Minimum number of replicas to allow scaling."
}

variable "replica_scale_cpu" {
  type        = number
  default     = 70
  description = "CPU usage to trigger autoscaling."
}

variable "replica_scale_in_cooldown" {
  type        = number
  default     = 300
  description = "Cooldown in seconds before allowing further scaling operations after a scale in."
}

variable "replica_scale_out_cooldown" {
  type        = number
  default     = 300
  description = "Cooldown in seconds before allowing further scaling operations after a scale out."
}

variable "performance_insights_enabled" {
  type        = bool
  default     = false
  description = "Specifies whether Performance Insights is enabled or not."
}

variable "performance_insights_kms_key_id" {
  type        = string
  default     = ""
  description = "The ARN for the KMS key to encrypt Performance Insights data."
}

variable "iam_database_authentication_enabled" {
  type        = bool
  default     = true
  description = "Specifies whether IAM Database authentication should be enabled or not. Not all versions and instances are supported. Refer to the AWS documentation to see which versions are supported."
}

variable "aws_security_group" {
  type        = list(string)
  default     = []
  description = "Specifies whether IAM Database authentication should be enabled or not. Not all versions and instances are supported. Refer to the AWS documentation to see which versions are supported."
}

variable "enabled_cloudwatch_logs_exports" {
  type        = list(string)
  default     = []
  description = "List of log types to export to cloudwatch. If omitted, no logs will be exported. The following log types are supported: audit, error, general, slowquery, postgresql (PostgreSQL)."
}

variable "availability_zone" {
  type        = string
  default     = ""
  description = "The Availability Zone of the RDS instance."
}


variable "enabled_subnet_group" {
  type        = bool
  default     = true
  description = "Set to false to prevent the module from creating any resources."
}

variable "enabled_rds_cluster" {
  type        = bool
  default     = true
  description = "Set to false to prevent the module from creating any resources."
}

variable "postgresql_family" {
  type        = string
  default     = "aurora-postgresql13"
  description = "The family of the DB parameter group."
}

variable "mysql_family" {
  type        = string
  default     = "aurora-mysql5.7"
  description = "The family of the DB parameter group."
}

variable "enable" {
  type        = bool
  default     = true
  description = "Set to false to prevent the module from creating any resources."
}

variable "postgresql_family_serverless" {
  type        = string
  default     = "aurora-postgresql10"
  description = "The family of the DB parameter group."
}

variable "mysql_family_serverless" {
  description = "The family of the DB parameter group."
  type        = string
  default     = "aurora5.6"
}

variable "enable_serverless" {
  description = "use serverless mode"
  type        = bool
  default     = false
}

variable "db_iam_roles" {
  description = "db iam roles"
  type        = list(string)
  default     = []
}

variable "availability_zones" {
  type        = list(any)
  default     = []
  description = "list of availability zones"
}

variable "enable_http_endpoint" {
  description = "Enable HTTP endpoint for serverless."
  type        = bool
  default     = true
}

variable "min_capacity" {
  description = "The minimum capacity."
  type        = number
  default     = 1
}

variable "storage_encrypted" {
  type        = bool
  default     = true
  description = "Enable underlying storage encryption."
}

variable "allow_major_version_upgrade" {
  description = "Enable to allow major engine version upgrades when changing engine versions. Defaults to `false`"
  type        = bool
  default     = false
}
variable "max_capacity" {
  description = "The maximum capacity."
  type        = number
  default     = 2
}
variable "db_port" {
  type        = string
  default     = ""
  description = "tcp port for the db"
}
