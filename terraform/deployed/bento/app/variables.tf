
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
variable "profile" {
  description = "iam user profile to use"
  type = string
}
variable "app_port" {
  description = "port on which the app listens"
  type = number
}

variable "domain_name" {
  description = "domain name for the application"
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
variable "app_name" {
  description = "name of the application"
  type = string
}

variable "fronted_instance_type" {
  description = "what size of instance to run"
  type = string
}
variable "min_size" {
  description = "minimum number of asg instances"
  type = number
}
variable "max_size" {
  description = "maximum number of asg instances"
  type = number
}
variable "enable_autoscaling" {
  description = "set to enable autoscaling"
  type = bool
  default = true
}
variable "health_check_type" {
  description = "The type of health check to use"
  type = string
  default = "EC2"
}
variable "associate_public_ip_address" {
  type = bool
  default = false
  description = "options to associate public ip to launched instances"
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
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
}
variable "desired_ec2_instance_capacity" {
  description = "number of ec2 to run workload ideally"
  type = number
}
variable "alb_port" {
  description = "Alb port to use in forwarding traffic to asg"
  type = number
  default = 80
}
variable "frontend_asg_name" {
  description = "name of the autoscalling group"
  type = string
  default = "front"
}

variable "env" {
  description = "environment"
  type = string
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
variable "platform" {
  type = string
  description = "name of the cloud platform e.g aws,gcp etc"
  default = "aws"
}

variable "vpc_cidr_block" {
  description = "CIDR Block for this  VPC. Example 10.0.0.0/16"
  default = "10.10.0.0/16"
  type = string
}


variable "alb_name" {
  description = "Name for the ALB"
  type = string
  default = "alb"
}
variable "create_alb" {
  description = "choose to create alb or not"
  type = bool
  default = true
}
variable "lb_type" {
  description = "Type of loadbalance"
  type = string
  default = "application"
}
variable "internal_alb" {
  description = "is this alb internal?"
  default = false
  type = bool
}

variable "ssl_policy" {
  description = "specify ssl policy to use"
  default = "ELBSecurityPolicy-2016-08"
  type = string
}
variable "default_message" {
  description = "default message response from alb when resource is not available"
  default = "The requested resource is not available"
}

variable "public_subnets" {
  description = "Provide list of public subnets to use in this VPC. Example 10.0.1.0/24,10.0.2.0/24"
  default     = []
  type = list(string)
}

variable "private_subnets" {
  description = "Provide list private subnets to use in this VPC. Example 10.0.10.0/24,10.0.11.0/24"
  default     = []
  type = list(string)
}

variable "availability_zones" {
  description = "list of availability zones to use"
  type = list(string)
  default = []
}

variable "alb_rule_priority" {
  description = "priority number to assign to alb rule"
  type = number
}


variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
}
variable "db_instance_volume_size" {
  description = "volume size of the instances"
  type = number
}

variable "database_name" {
  description = "name of the database"
  type = string
}
variable "database_instance_type" {
  description = "ec2 instance type to use"
  type = string
}
variable "database_password" {
  description = "set database password"
  type = string
  default = "custodian"
}
variable "db_private_ip" {
  description = "private ip of the db instance"
  type = string
}
variable "remote_state_bucket_name" {
  description = "name of the terraform remote state bucket"
  type = string
}
variable "redis_node_group" {
  description = "number of redis nodes"
  type = string
}
variable "shutdown_schedule" {
  type    = "string"
  default = "0 5 * * *"
}

variable "startup_schedule" {
  type    = "string"
  default = "0 12 * * MON-FRI"
}