output "centos_ami_id" {
  value = "${data.aws_ami.centos.id}"
}