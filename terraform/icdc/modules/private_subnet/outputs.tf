output "private_subnet_id" {
  value = "${aws_subnet.private_subnet.id}"
}

output "subnet_name" {
  value = "${var.subnet_name}"
}

output "private_az" {
  value = "${var.private_az}"
}
output "private_route_table_id" {
  value = "${aws_route_table.private_route.id}"
}
