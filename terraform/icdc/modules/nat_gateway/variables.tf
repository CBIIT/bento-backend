
variable "nat_instance_type" {
  default = "t2.micro"
}

variable "public_subnet_name" {}
variable "public_subnet_id" {}
variable "public_subnet_az" {}
variable "private_subnet_cidr" {
  type = "list"
}
variable "vpc_id" {}

# variable "private_subnet_route_ids" {
#   type = "list"
#   default = []
# }
variable "private_subnet_count" {
  default = 1
}
variable "user" {
  default = "jenkins"
}
variable "ssh_key_name" {}
variable "ami_id" {}
variable "insecure_no_strict_host_key_checking" {
  default = false
}
variable "private_subnet_route_ids" {
  type = "list"
  default = []
}
variable "neo4j_home" {
  default = "/local/neo4j"  
}

