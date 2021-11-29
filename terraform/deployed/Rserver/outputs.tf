output "rds_password" {
  value = random_password.rds_password.result
}