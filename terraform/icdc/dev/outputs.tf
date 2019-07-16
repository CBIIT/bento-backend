output "vpc_name" {
  value = "${module.sandbox_vpc.vpc_name}"
}
output "vpc_id" {
  value = "${module.sandbox_vpc.vpc_id}"
}
output "vpc_region" {
  value = "${module.sandbox_vpc.region}"
}


#Output Public Subnet details
output "public_subnet_a_id" {
  value = "${module.sandbox_public_subnet_a.public_subnet_id}"
}
output "public_subnet_c_id" {
  value = "${module.sandbox_public_subnet_c.public_subnet_id}"
}
output "public_subnet_a_name" {
  value = "${module.sandbox_public_subnet_a.public_subnet_name}"
}
output "public_subnet_c_name" {
  value = "${module.sandbox_public_subnet_c.public_subnet_name}"
}
output "db_private_subnent_a_id" {
  value = "${module.sandbox_private_subnet_a_db.private_subnet_id}"
}
output "app_private_subnent_a_id" {
  value = "${module.sandbox_private_subnet_a_app.private_subnet_id}"
}
output "public_az_a" {
  value = "${module.sandbox_public_subnet_a.public_az}"
}
output "public_az_c" {
  value = "${module.sandbox_public_subnet_c.public_az}"
}
output "app_security_id" {
  value = "${module.sandbox_security_groups.app_security_group_id}"
}

output "db_security_id" {
  value = "${module.sandbox_security_groups.db_security_group_id}"
}
output "docker_security_id" {
  value = "${module.sandbox_security_groups.docker_security_group_id}"
}
output "public_security_id" {
  value = "${module.sandbox_security_groups.public_security_group_id}"
}
output "bastion_security_id" {
  value = "${module.sandbox_security_groups.bastion_security_group_id}"
}
output "jenkins_security_id" {
  value = "${module.sandbox_security_groups.jenkins_security_group_id}"
}
output "centos_ami" {
  value = "${module.centos_ami.centos_ami_id}"
}


output "ssh_keypair" {
  value = "${aws_key_pair.keypair.key_name}"
}
# output "icdc_jenkins_eip" {
#   value = "${module.sandbox_eip.icdc_jenkins_eip}"
# }

# output "icdc_neo4j_eip" {
#   value = "${module.sandbox_eip.icdc_neo4j_eip}"
# }

# output "icdc_bastion_eip" {
#   value = "${module.sandbox_eip.icdc_bastion_eip}"
# }
# output "icdc_k9dc_eip" {
#   value = "${module.sandbox_eip.icdc_k9dc_eip}"
# }

# output "icdc_docker_eip" {
#   value = "${module.sandbox_eip.icdc_docker_eip}"
# }
output "bastion_public_ip" {
  value = "${aws_eip_association.icdc_bastion_eip.public_ip}"
}
output "alb_arn" {
  value = "${module.sandbox_alb.alb_arn}"
}
output "alb_dns" {
  value = "${module.sandbox_alb.alb_dns}"
}
output "alb_zone_id" {
  value = "${module.sandbox_alb.alb_zone_id}"
}
output "zone_id" {
  value = "${module.sandbox_alb.alb_zone_id}"
}
output "deployment_bucket_name" {
  value = "${aws_s3_bucket.sandbox_deployment_buckets.bucket}"
}
output "s3_role_profile_name" {
  value = "${module.sandbox_s3_ami_role.s3_role_profile_name}"
}
