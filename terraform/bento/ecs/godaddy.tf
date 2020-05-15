<<<<<<< HEAD
#data "aws_ssm_parameter" "dns_api_key" {
#  name = "dns_api_key"
#}
#data "aws_ssm_parameter" "dns_api_secret" {
#  name = "dns_api_secret"
#}
#provider "godaddy" {
#    key = data.aws_ssm_parameter.dns_api_key.value
#    secret = data.aws_ssm_parameter.dns_api_secret.value
#}
=======
data "aws_ssm_parameter" "dns_api_key" {
  name = "dns_api_key"
}
data "aws_ssm_parameter" "dns_api_secret" {
  name = "dns_api_secret"
}
provider "godaddy" {
    key = data.aws_ssm_parameter.dns_api_key.value
    secret = data.aws_ssm_parameter.dns_api_secret.value
}
>>>>>>> b9b506ce97e3b5d8317edd028dc7d20d4467d569

# resource "godaddy_domain_record" "essential-dev" {
#   domain    = var.domain
#   record {
#     name = join("-",[var.svc_ecs,var.stack_name])
#     type = "CNAME"
#     data = aws_alb.alb.dns_name
#     ttl = 3600
#   }
# }
