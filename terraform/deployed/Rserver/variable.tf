variable "stack_name" {
  description = "name of the project"
  type = string
}
variable "env" {
  description = "environment"
  type = string
}
variable "tags" {
  description = "tags to associate with this instance"
  type = map(string)
}
variable "remote_state_bucket_name" {
  description = "name of the terraform remote state bucket"
  type = string
}
variable "region" {
  type        = string
  description = "AWS region"
}
variable "ecs_cluster_name" {
  description = "name of the ecs cluster"
}

variable "container_replicas" {
  description = "specify the number of container to run"
  type = number
}
variable "frontend_container_port" {
  description = "port on which the container listens"
  type = number
}
variable "instance_volume_size" {
  default = ""
}
variable "evs_volume_type" {
  description = "EVS volume type"
  default = "standard"
  type = string
}
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
}
variable "associate_public_ip_address" {
  type = bool
  default = false
  description = "options to associate public ip to launched instances"
}
variable "fronted_instance_type" {
  description = "what size of instance to run"
  type = string
}
variable "health_check_type" {
  description = "The type of health check to use"
  type = string
  default = "EC2"
}
variable "frontend_asg_name" {
  description = "name of the autoscalling group"
  type = string
  default = "front"
}
variable "shutdown_schedule" {
  type    = string
  default = "0 5 * * *"
}
variable "startup_schedule" {
  type    = string
  default = "0 12 * * MON-FRI"
}
variable "desired_ec2_instance_capacity" {
  description = "number of ec2 to run workload ideally"
  type = number
}
variable "min_size" {
  description = "minimum number of asg instances"
  type = number
}
variable "max_size" {
  description = "maximum number of asg instances"
  type = number
}
variable "fronted_rule_priority" {
  description = "priority number to assign to alb rule"
  type = number
  default = 100
}

variable "profile" {
  description = "iam user profile to use"
  type = string
}

variable "domain_name" {
  description = "domain name for the application"
  type = string
}
variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
}

variable "database_name" {
  type        = string
  description = "The name of the database to create when the DB instance is created"
}

variable "database_user" {
  type        = string
  description = "Username for the master DB user"
}
variable "multi_az" {
  type        = bool
  description = "Set to true if multi AZ deployment must be supported"
}

variable "storage_type" {
  type        = string
  description = "One of 'standard' (magnetic), 'gp2' (general purpose SSD), or 'io1' (provisioned IOPS SSD)"
}
variable "storage_encrypted" {
  type        = bool
  description = "(Optional) Specifies whether the DB instance is encrypted. The default is false if not specified"
}

variable "allocated_storage" {
  type        = number
  description = "The allocated storage in GBs"
}

variable "engine" {
  type        = string
  description = "Database engine type"
  # http://docs.aws.amazon.com/cli/latest/reference/rds/create-db-instance.html
  # - mysql
  # - postgres
  # - oracle-*
  # - sqlserver-*
}

variable "engine_version" {
  type        = string
  description = "Database engine version, depends on engine type"
  # http://docs.aws.amazon.com/cli/latest/reference/rds/create-db-instance.html
}
variable "instance_class" {
  type        = string
  description = "Class of RDS instance"
  # https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.DBInstanceClass.html
}
variable "publicly_accessible" {
  type        = bool
  description = "Determines if database can be publicly available (NOT recommended)"
  default = false
}
variable "apply_immediately" {
  type        = bool
  description = "Specifies whether any database modifications are applied immediately, or during the next maintenance window"
}

variable "identifier" {
  type        = string
  description = "Name  (e.g. `app` or `cluster`)"
}
variable "db_subnet_id_name" {
  type        = string
  description = "name for db_subnet_id_name"
}