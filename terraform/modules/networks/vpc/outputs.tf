output "vpc_id" {
  description = "VPC Id"
  value = aws_vpc.vpc.id
}

output "private_subnets_ids" {
  description = "private subnets ids"
  value    = aws_subnet.private_subnet.*.id
}

output "public_subnets_ids" {
  description = "public subnets ids"
  value       = aws_subnet.public_subnet.*.id
}

output "database_subnets_ids" {
  description = "database subnets ids"
  value       = aws_subnet.db_subnet.*.id
}
