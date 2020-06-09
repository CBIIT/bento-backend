
locals {
  devops_ssh_key_pub = "${secret_resource.devops_ssh_key_public.value}"
  devops_user = "${secret_resource.devops_user.value}"
  devops_ssh_key     = "${secret_resource.devops_ssh_key_private.value}"
}
#Let's provision bastion hose
resource "aws_instance" "bastion_host" {
  ami                         = "${module.centos_ami.centos_ami_id}"
  instance_type               = "${var.bastion_instance_type}"
  vpc_security_group_ids      = ["${module.sandbox_security_groups.bastion_security_group_id}"]
  key_name                    = "${aws_key_pair.keypair.key_name}"
  subnet_id                   = "${module.sandbox_public_subnet_a.public_subnet_id}"
  source_dest_check           = false
  tags = {
        Name                    = "icdc_bastion"
        Org                     = "icdc"
        ByTerraform             = "true"
  }
  user_data                   = <<-EOF
                              #cloud-config
                              users:
                                - name: "${local.devops_user}"
                                  gecos: "${local.devops_user}"
                                  sudo: ALL=(ALL) NOPASSWD:ALL
                                  groups: wheel
                                  shell: /bin/bash
                                  ssh_authorized_keys:
                                  - "${local.devops_ssh_key_pub}"
                                  EOF
connection {
    user = "${local.devops_user}"
    private_key  = "${local.devops_ssh_key}"
  }
#  provisioner "ansible" {
#     plays {
#       playbook = {
#         file_path = "../playbook/nat.yml"
#         roles_path = ["../roles"]
#       }
#       verbose = false
#     }
#     ansible_ssh_settings {
#       insecure_no_strict_host_key_checking = "${var.insecure_no_strict_host_key_checking}"
#       connect_timeout_seconds = 60
#     }
#   }
}

data "aws_eip" "icdc_bastion" {
  tags = {
    Name = "icdc_nat_bastion"
  }
}


resource "aws_eip_association" "icdc_bastion_eip" {
  instance_id   = "${aws_instance.bastion_host.id}"
  allocation_id = "${data.aws_eip.icdc_bastion.id}"
}

data "aws_route_tables" "private_routes" {
  vpc_id = "${module.sandbox_vpc.vpc_id}"

  tags = {
    Type = "private"
  }
  depends_on = ["module.sandbox_private_subnet_a_app","module.sandbox_private_subnet_a_db"]
}
resource "aws_route" "private_subnets_route" {
  #count = number of private subnets
  # count                     = "${length(data.aws_route_tables.private_routes.ids)}"
  count = 4
  route_table_id            = "${data.aws_route_tables.private_routes.ids[count.index]}"
  destination_cidr_block    = "0.0.0.0/0"
  instance_id               = "${aws_instance.bastion_host.id}"
  
}