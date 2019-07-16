output "icdc_nat_bastion_id" {
  value = "${aws_instance.nat_bastion.id}"
}
output "nat_bastion_public_ip" {
  value = "${aws_eip_association.icdc_nat_bastion_eip.public_ip}"
}