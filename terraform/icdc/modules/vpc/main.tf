resource "aws_vpc" "vpc" {
  cidr_block                = "${var.vpc_cidr}"
  enable_dns_hostnames      = true

  tags = {
    Name                    = "${var.vpc_name}"
    Terraform               = "true"
  }
}
#create internet gateway
resource "aws_internet_gateway" "igw" {
  vpc_id                        = "${aws_vpc.vpc.id}"
  tags = {
    Name                        = "${var.igw_name}"
    Terraform                   = "true"
  }
}
#public route table
resource "aws_route_table" "public_route" {
  vpc_id                        = "${aws_vpc.vpc.id}"
  
  tags = {
    Name                        = "route_${var.vpc_name}"
  }
}