
variable "profile" {
  description = "Profile for launching vm"
  default = "icdc"
}

variable "region" {
  description = "Region to provision resources"
  default     = "us-east-1"
}
variable "public_az_a" {
  description = "availability zone for public subnet us-east-1a"
  default = "us-east-1a"
}
variable "public_az_c" {
  description = "availability zone for public subnet us-east-1c"
  default = "us-east-1c"
}
variable "private_az_a" {
  description = "availability zone for private subnet us-east-1a"
  default = "us-east-1a"
}
variable "private_az_c" {
  description = "availability zone for private subnet us-east-1a"
  default = "us-east-1c"
}
variable "public_subnet_a" {
  description = "Public subnet us-east-1a"
  default = "172.18.0.0/24"
}
variable "public_subnet_c" {
  description = "Public subnet us-east-1c"
  default = "172.18.2.0/24"
}

variable "private_subnet_a_app" {
  description = "Private subnet us-east-1a"
  default = "172.18.1.0/24"
}
variable "private_subnet_c_app" {
  description = "Private subnet us-east-1c"
  default = "172.18.3.0/24"
}
variable "private_subnet_a_db" {
  description = "Private subnet us-east-1a"
  default = "172.18.4.0/24"
}
variable "private_subnet_c_db" {
  description = "Private subnet us-east-1c"
  default = "172.18.5.0/24"
}

variable "vpc_cidr" {
  description = "VPC CIDR to use for your environment"
  default = "172.18.0.0/16"
}

variable "public_subnet_name_c" {
  default = "sandbox_icdc_public_subnet_c"
}
variable "public_subnet_name_a" {
  default = "sandbox_icdc_public_subnet_a"
}
variable "private_subnet_a_name_app" {
  default = "sandbox_icdc_private_subnet_a_app"
}
variable "private_subnet_c_name_app" {
  default = "sandbox_icdc_private_subnet_c_app"
}
variable "private_subnet_a_name_db" {
  default = "sandbox_icdc_private_subnet_a_db"
}
variable "private_subnet_c_name_db" {
  default = "sandbox_icdc_private_subnet_c_db"
}
variable "app_security_group_name" {
  default = "icdc_sandbox_app_sg"
}
variable "db_security_group_name" {
  default = "icdc_sandbox_db_sg"
}
variable "alb_security_group_name" {
  default = "icdc_sandbox_alb_sg"
}
variable "jenkins_security_group_name" {
  default = "icdc_sandbox_jenkins_sg"
}
variable "public_security_group_name" {
  default = "icdc_sandbox_public_sg"
}
variable "docker_security_group_name" {
  default = "icdc_sandbox_docker_sg"
}
variable "bastion_security_group_name" {
  default = "icdc_sandbox_bastion_sg"
}

variable "environment" {
  description  = "name of the environment, example sandbox,qa,etc"
  default = "sandbox"
}
variable "terraform-s3-bucket-name" {
  default   = "icdc-sandbox-terraform-state"
  description = "name of the s3 bucket to store terraform remote state"
}
variable "keyname" {
  default = "icdc_devops"
}
variable "bastion_instance_type" {
  default = "t2.medium"
}
variable "s3_bucket_name" {
  default = "icdc-sandbox-s3-alb-log"
}
variable "alb_name" {
  default = "icdc-sandbox-alb"
}
variable "domain_name" {
  default = "essential-dev.com"
}
variable "insecure_no_strict_host_key_checking" {
  default = false
}
variable "deployments_bucket_name" {
  default = "icdc-sandbox-deployments"
}
variable "role_name" {
  default = "icdc-ec2-s3-access-role"
}
variable "policy_name" {
  default = "icdc-s3-role-policy"
}
variable "attach_policy_name" {
  default = "icdc-s3-iam-policy-attach"
}
variable "profile_name" {
  default = "icdc-s3-role-profile"
}