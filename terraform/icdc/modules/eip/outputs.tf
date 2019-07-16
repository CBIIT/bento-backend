# output "icdc_jenkins_eip" {
#   value = "${data.aws_eip.icdc_jenkins.id}"
# }

# output "icdc_k9dc_eip" {
#   value = "${data.aws_eip.icdc_k9dc.id}"
# }

# output "icdc_docker_eip" {
#   value = "${data.aws_eip.icdc_docker.id}"
# }
# output "icdc_neo4j_eip" {
#   value = "${data.aws_eip.icdc_neo4j.id}"
# }

output "icdc_bastion_eip" {
  value = "${data.aws_eip.icdc_bastion.id}"
}