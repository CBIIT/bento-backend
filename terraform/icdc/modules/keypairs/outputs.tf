
output "devops_keypair" {
  value = "${aws_key_pair.keypair.key_name}"
}