#create public subnet(s)
resource "aws_subnet" "public_subnet" {
  cidr_block                    = "${var.public_subnet_cidr}"
  vpc_id                        = "${var.vpc_id}"
  map_public_ip_on_launch       = true
  availability_zone             = "${var.public_az}"

  tags = {
    Name                        = "${var.subnet_name}"
    Terraform                   = "true"
  }
}

#internet access
resource "aws_route" "internet_access" {
  route_table_id         = "${var.route_id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = "${var.igw_id}"

}

# Associate the routing table to public subnet
resource "aws_route_table_association" "route_table_association" {
  subnet_id                     = "${aws_subnet.public_subnet.id}"
  route_table_id         = "${var.route_id}"
}

