variable "tags" {
  description = "tags for the vpc"
  type = map(string)
}
variable "vpc_cidr_block" {
  description = "CIDR Block for this  VPC. Example 10.0.0.0/16"
  default = "10.0.0.0/16"
  type = string
}
variable "stack_name" {
  description = "Name of project. Example arp"
  type = string
  default = "main"
}
variable "custom_vpc_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_igw_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_private_tags" {
  description = "Custom tags for the private subnet"
  type = map(string)
  default = {}
}
variable "custom_public_tags" {
  description = "Custom tags for the public subnet"
  type = map(string)
  default = {}
}
variable "custom_db_tags" {
  description = "Custom tags for the database subnet"
  type = map(string)
  default = {}
}
variable "custom_nat_gateway_tags" {
  description = "Custom tags for the database subnet"
  type = map(string)
  default = {}
}
variable "custom_db_subnet_group_tags" {
  description = "Custom tags for the database subnet group"
  type = map(string)
  default = {}
}
variable "enable_hostname_dns" {
  description = "use true or false to determine support for hostname dns"
  type = bool
  default = true
}
variable "instance_tenancy" {
  description = "instances tenancy option. Options are dedicated or default"
  default     = "default"
  type = string
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

variable "db_subnets" {
  description = "Provide list database subnets to use in this VPC. Example 10.0.20.0/24,10.0.21.0/24"
  type        =  list(string)
  default     = []
}
variable "create_vpc" {
  description = "Use true or false to determine if a new vpc is to be created"
  type = bool
  default = true
}
variable "env" {
  description = "specify environment for this vpc"
  type = string
  default = ""
}
variable "single_nat_gateway" {
  description = "Choose as to wherether you want single Nat Gateway for the environments or multiple"
  type        = bool
  default     = true
}
variable "one_nat_gateway_per_az" {
  description = "Choose as to wherether you want one Nat Gateway per availability zone or not"
  type        = bool
  default     = false
}
variable "availability_zones" {
  description = "list of availability zones to use"
  type = list(string)
  default = []
}
variable "create_db_subnet_group" {
  description = "Set to true if you want to create database subnet group for RDS"
  type = bool
  default = true
}
variable "name_db_subnet_group" {
  default = "db-subnet"
  type = string
  description = "name of the db subnet group"
}

variable "reuse_nat_ips" {
  description = "Choose wherether you want EIPs to be created or not"
  type        = bool
  default     = false
}

variable "external_nat_ip_ids" {
  description = "List of EIP to be assigned to the NAT Gateways if you don't want to don't want to reuse existing EIP"
  type        = list(string)
  default     = []
}
variable "enable_nat_gateway" {
  description = "choose as to  provision NAT Gateways for each of your private subnets"
  type        = bool
  default     = true
}
variable "custom_public_route_table_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_private_route_table_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_private_subnet_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_public_subnet_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "custom_db_subnet_tags" {
  description = "Custom tags for the vpc"
  type = map(string)
  default = {}
}
variable "enable_dns_support" {
  description = "enable dns resolution"
  type = bool
  default = true
}
