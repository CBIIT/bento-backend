variable "instance_type" {
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
variable "tags" {
  description = "provide custom tags"
  type = map(string)
  default = {}
}
variable "enable_autoscaling" {
  description = "set to enable autoscaling"
  type = bool
  default = true
}
variable "ami" {
  description = "ami to use"
  default = null
  type = string
}
variable "stack_name" {
  description = "stack name"
  type = string
}

variable "subnets" {
  description = "subnets to deploy "
  default = []
  type = list(string)
}
variable "target_group_arn" {
  description = "The ARN of the ALB"
  type = list(string)
  default = []
}
variable "user_data" {
  description = "The user data script"
  type = string
  default = null
}
variable "health_check_type" {
  description = "The type of health check to use"
  type = string
  default = "EC2"
}
variable "vpc_subnet" {
  default = []
  type = list(string)
  description = "subnet to locate vpc id"
}
variable "instance_profile" {
  type = string
  description = "instance profile to assign"
}
variable "security_groups_ids" {
  description = "List of security groups ids"
  default = []
  type = list(string)
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
  default = 40
  type = number
}
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
}
variable "desired_ec2_instance_capacity" {
  description = "number of ec2 to run workload ideally"
  type = number
  default = 1
}
variable "vpc_id" {
  description = "id of the vpc to use"
  type = string
}
variable "bastion_security_group_id" {
  description = "basion host security group id"
  type = string
  default = ""
}
variable "alb_port" {
  description = "Alb port to use in forwarding traffic to asg"
  type = number
  default = 80
}
variable "alb_security_group_id" {
  description = "asg security group id"
  default = ""
  type = string
}
variable "asg_name" {
  description = "name of the autoscalling group"
  type = string
}
variable "private_subnets_block" {
  description = "private subnets"
  type = list(string)
  default = []
}
variable "env" {
  description = "environment"
  type = string
}
