
output "bastion_security_group_id" {
  value = data.terraform_remote_state.network.outputs
}

output "bastion_ip" {
  description = "ip address of bastion host"
  value       = data.terraform_remote_state.network.outputs.bastion_public_ip
}

output "alb_arn" {
  value = aws_alb.alb.arn
}
output "alb_dns" {
  value = aws_alb.alb.dns_name
}
output "zone_id" {
  value = aws_alb.alb.zone_id
}