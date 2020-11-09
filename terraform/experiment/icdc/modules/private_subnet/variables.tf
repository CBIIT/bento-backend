
variable "vpc_id" {}
variable "private_subnet_cidr" {}
variable "private_az" {}
variable "subnet_name" {
    default = "icdc_private_subnet"
}

