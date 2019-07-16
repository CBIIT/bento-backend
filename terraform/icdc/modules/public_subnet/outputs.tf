output "public_subnet_id" {
  value = "${aws_subnet.public_subnet.id}"
}

output "public_subnet_name" {
  value = "${var.subnet_name}"
}

output "public_az" {
  value = "${var.public_az}"
}
