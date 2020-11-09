output "public_subnets_ids" {
  value = module.vpc.public_subnets_ids
}
output "vpc_id" {
  value = module.vpc.vpc_id
}
output "private_subnets_ids" {
  value = module.vpc.private_subnets_ids
}
output "private_subnets" {
  value = var.private_subnets
}