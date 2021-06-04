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

output "vpc_cidr_block"{
  description="cidr block details of vpc"
  value = module.vpc.vpc_cidr_block
}