variable "region" {
  type        = string
  description = "AWS region"
}

variable "availability_zones" {
  type = list(string)
}

variable "stage" {
  type        = string
  description = "Stage (e.g. `prod`, `dev`, `staging`, `infra`)"
}

variable "name" {
  type        = string
  description = "Name  (e.g. `app` or `cluster`)"
}
variable "identifier" {
  type        = string
  description = "Name  (e.g. `app` or `cluster`)"
}
variable "database_name" {
  type        = string
  description = "The name of the database to create when the DB instance is created"
}

variable "database_user" {
  type        = string
  description = "Username for the master DB user"
}

variable "database_password" {
  type        = string
  description = "Password for the master DB user"
}

variable "database_port" {
  type        = number
  description = "Database port (_e.g._ `3306` for `MySQL`). Used in the DB Security Group to allow access to the DB instance from the provided `security_group_ids`"
}

variable "deletion_protection" {
  type        = bool
  description = "Set to true to enable deletion protection on the RDS instance"
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

variable "db_parameter_group" {
  type        = string
  description = "Parameter group, depends on DB engine used"
}

variable "publicly_accessible" {
  type        = bool
  description = "Determines if database can be publicly available (NOT recommended)"
}

variable "apply_immediately" {
  type        = bool
  description = "Specifies whether any database modifications are applied immediately, or during the next maintenance window"
}

variable "subnet_ids" {
  description = "A list of VPC subnet IDs"
  default     = []
  type        = list(string)
}
variable "rds_private_subnets" {
  description = "List of VPC rds_private_subnets"
  default     = []
  type        = list(string)
}
variable "security_group_ids" {
  type        = list(string)
  default     = []
  description = "The IDs of the security groups from which to allow `ingress` traffic to the DB instance"
}

variable "vpc_id" {
  type        = string
  default     = ""
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
variable "env" {
  description = "environment"
  type = string
}

variable "rds_public_subnets" {
  default = ""
}
variable "rds_vpc_cidr" {
  default = ""
}

variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
}
variable "remote_state_bucket_name" {
  description = "name of the terraform remote state bucket"
  type = string
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
variable "backend_container_port" {
  description = "port on which the container listens"
  type = number
}
variable "profile" {
  description = "iam user profile to use"
  type = string
}

variable "domain_name" {
  description = "domain name for the application"
  type = string
}



#autoscaling variables:
variable "startup_schedule" {
  type    = string
  default = "0 12 * * MON-FRI"
}

variable "fronted_instance_type" {
  description = "what size of instance to run"
  type = string
}
variable "associate_public_ip_address" {
  type = bool
  default = false
  description = "options to associate public ip to launched instances"
}
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
}
variable "evs_volume_type" {
  description = "EVS volume type"
  default = "standard"
  type = string
}
variable "instance_volume_size" {
  description = "volume size of the instances"
  type = number
}
variable "frontend_asg_name" {
  description = "name of the autoscalling group"
  type = string
  default = "front"
}
variable "min_size" {
  description = "minimum number of asg instances"
  type = number
}
variable "max_size" {
  description = "maximum number of asg instances"
  type = number
}
variable "desired_ec2_instance_capacity" {
  description = "number of ec2 to run workload ideally"
  type = number
}
variable "health_check_type" {
  description = "The type of health check to use"
  type = string
  default = "EC2"
}
variable "shutdown_schedule" {
  type    = string
  default = "0 5 * * *"
}
variable "fronted_rule_priority" {
  description = "priority number to assign to alb rule"
  type = number
  default = 100
}
variable "backend_rule_priority" {
  description = "priority number to assign to alb rule"
  type = number
  default = 110
}

variable "database_instance_type" {
  default = ""
}
variable "db_private_ip" {
  default = ""
}
variable "db_instance_volume_size" {
  default = ""
}