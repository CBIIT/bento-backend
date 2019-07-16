#create private subnet
resource "aws_subnet" "private_subnet" {
  cidr_block                    = "${var.private_subnet_cidr}"
  vpc_id                        = "${var.vpc_id}"
  availability_zone             = "${var.private_az}"

  tags = {
    Name                        = "${var.subnet_name}"
    Type                        = "private"
  }
}

#private route table
resource "aws_route_table" "private_route" {
  vpc_id                        = "${var.vpc_id}"

  tags {
    Name                        = "${var.subnet_name}"
    Type                        = "private"
  }
}

# Associate the routing table to private subnet
resource "aws_route_table_association" "route_table_association" {
  subnet_id                     = "${aws_subnet.private_subnet.id}"
  route_table_id                = "${aws_route_table.private_route.id}"
}
