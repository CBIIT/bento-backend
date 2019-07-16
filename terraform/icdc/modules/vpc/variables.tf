variable "vpc_cidr" {}
variable "vpc_name" {}
variable "vpc_region" {
  default = "us-east-1"
}
variable "igw_name" {
  default = "icdc_internet_gateway"
}



# variable "vpc_cidr" {
#   default = "172.20.0.0/16"
# }
# variable "vpc_id" {}
# variable "public_subnet_cidr" {
#   default = "172.20.0.0/24"
# }

# variable "private_subnet_cidr" {
#   default = "172.20.1.0/24"
# }

# variable "az-subnet-maps" {
#   type        = "list"
#   description = "List of availability zones we wish to create"

#   default = [
#     {
#       name = "icdc_public_subnet"
#       az   = "us-east-1a"
#       cidr = "172.20.0.0/24"
#     },
#     {
#       name = "icdc_private_subnet"
#       az   = "us-east-1c"
#       cidr = "172.20.1.0/24"
#     },
#   ]
# }