output "vpc_name" {
  value = "${var.vpc_name}"
}

output "vpc_id" {
  value = "${aws_vpc.vpc.id}"
}

output "region" {
  value = "${var.vpc_region}"
}
output "igw_id" {
  value = "${aws_internet_gateway.igw.id}"
}
output "route_id" {
  value = "${aws_route_table.public_route.id}"
}
