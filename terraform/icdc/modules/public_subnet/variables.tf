
variable "vpc_id" {}
variable "public_subnet_cidr" {}
variable "public_az" {}
variable "subnet_name" {
    default = "icdc_public_subnet"
}
variable "igw_id" {
  
}
variable "route_id" {}
