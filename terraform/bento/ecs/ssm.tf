resource "aws_ssm_parameter" "vault" {
  name        = "vault_password"
  description = "vault password"
  type        = "SecureString"
  value       = local.vault_password
  overwrite   = true
  tags = {
    environment = var.stack_name
  }
}