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
variable "shutdown_schedule" {
  type    = string
  default = "0 5 * * *"
}

variable "startup_schedule" {
  type    = string
  default = "0 12 * * MON-FRI"
}
variable "ssh_user" {
  type = string
  description = "name of the ec2 user"
  default = "bento"
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
  default = true
  description = "options to associate public ip to launched instances"
}
variable "desired_ec2_instance_capacity" {
  description = "number of ec2 to run workload ideally"
  type = number
  default = 2
}
variable "evs_volume_type" {
  description = "EVS volume type"
  default = "standard"
  type = string
}
variable "instance_volume_size" {
  description = "volume size of the instances"
  type = number
  default = 50
}
variable "github_actions_instance_type" {
  description = "what size of instance to run"
  type = string
  default = "t3.small"
}
variable "asg_name" {
  description = "name of the autoscalling group"
  type = string
  default = "github-actionss"
}
variable "ssh_key_name" {
  description = "name of the ssh key to manage the instances"
  type = string
  default = "devops"
}

variable "instance_profile_name" {
  default = "bento-management-ecs-instance-profile"
  description = "iam instance profile name"
}
variable "remote_state_bucket_name" {
  description = "name of the terraform remote state bucket"
  type = string
  default = "bento-terraform-remote-state"
}
variable "github_actions_secret_name" {
  type = string
  description = "name of github actions token secret"
  default = "github-actions-secret"
}