output "management_route_tables" {
  value = data.aws_route_tables.management.ids
}
output "other_route_tables" {
  value = data.aws_route_tables.others.ids
}