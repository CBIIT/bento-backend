
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
variable "alb_rule_priority" {
  description = "priority number to assign to alb rule"
  type = number
}
variable "domain_name" {
  description = "domain name for the application"
  type = string
}

variable "app_name" {
  description = "name of the application"
  type = string
}
variable "app_instance_type" {
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
variable "app_asg_name" {
  description = "name of the autoscalling group"
  type = string
}

variable "env" {
  description = "environment"
  type = string
}
variable "availability_zone" {
  description = "availability zone to provision"
  type = string
}

variable "devops_user" {
  description = "user to create to access the instance"
  type = string
}