
output "bastion_security_group_id" {
  value = data.terraform_remote_state.network.outputs
}

output "bastion_ip" {
  description = "ip address of bastion host"
  value       = data.terraform_remote_state.network.outputs.bastion_public_ip
}