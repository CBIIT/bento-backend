output "public_security_group_id" {
  value = "${aws_security_group.public_security_group.id}"
}

output "db_security_group_id" {
  value = "${aws_security_group.db_security_group.id}"
}

output "app_security_group_id" {
  value = "${aws_security_group.app_security_group.id}"
}

output "docker_security_group_id" {
  value = "${aws_security_group.docker_security_group.id}"
}
output "bastion_security_group_id" {
  value = "${aws_security_group.bastion_security_group.id}"
}
output "jenkins_security_group_id" {
  value = "${aws_security_group.jenkins_security_group.id}"
}