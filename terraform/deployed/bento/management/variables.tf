variable "env" {
  description = "environment to build"
}
variable "stack_name" {
  description = "name of the project"
}
variable "mgt_public_subnets" {
  description = "specify list of public subnets"
}
variable "mgt_private_subnets" {
  description = "specify list of public subnets"
}
variable "mgt_vpc_cidr" {
  description = "cidr block to create vpc"
}
variable "mgt_availability_zones" {
  description = "list of AZ to use"
}
variable "region" {
  description = "aws region to use"
  type = string
}
variable "profile" {
  description = "user profile to use"
  type = string
}
variable "tags" {
  description = "tags to associate to resources"
  type = map(string)
  default = {}
}

variable "bastion_instance_type" {
  description = "ec2 instance type to use"
  type = string
}

variable "ssh_key_name" {
  description = "name of the ssh key "
  type = string
}
variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
}

variable "ec2_instance_type" {
  description = "ec2 instance type to use"
  type = string
}

variable "jenkins_name" {
  description = "name of the jenkins"
  type = string
  default = "jenkins"
}

variable "instance_volume_size" {
  description = "volume size of the instances"
  type = number
  default = "40"
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
variable "enable_autoscaling" {
  description = "set to enable autoscaling"
  type = bool
  default = true
}

variable "alb_priority_rule" {
  description = "alb priority rule"
  default = 100
  type = string
}
variable "domain_name" {
  description = "domain name"
  type = string
}
variable "jenkins_private_ip" {
  description = "set jenkins private ip"
  type = string
}
