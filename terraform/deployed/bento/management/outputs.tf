output "public_subnets_ids" {
  value = module.mgt-vpc.public_subnets_ids
}
output "vpc_id" {
  value = module.mgt-vpc.vpc_id
}
output "private_subnets_ids" {
  value = module.mgt-vpc.private_subnets_ids
}
output "private_subnets" {
  value = var.mgt_private_subnets
}
output "alb_security_group_id" {
  value = module.alb.alb_security_group_id
}
output "alb_dns_name" {
  value = module.alb.alb_dns_name
}
output "alb_zone_id" {
  value = module.alb.alb_zone_id
}
output "alb_https_listerner_arn" {
  value = module.alb.alb_https_listener_arn
}
output "bastion_subnet_id" {
  value = module.mgt-vpc.public_subnets_ids[0]
}
output "bastion_security_group_id" {
  value = aws_security_group.bastion-sg.id
}
output "dataloader_security_group_id" {
  value = aws_security_group.data-loader-sg.id
}
output "bento-jenkins-sg-id" {
  value = aws_security_group.jenkins-sg.id
}
output "jenkins_security_group_id" {
  value = aws_security_group.jenkins-sg.id
}
output "katalon-sg-id" {
  value = aws_security_group.katalon-sg.id
}