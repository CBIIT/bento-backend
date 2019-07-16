#Create nat gateway security group
# - ingress SSH from internet
# - ingress from private subnets
# - egress to internet for any traffic

# data "aws_route_tables" "route_table_ids" {
#   vpc_id = "${var.vpc_id}"

#   filter {
#     name   = "tag:Type"
#     values = ["private"]
#   }
# }
resource "aws_security_group" "icdc_nat_sg" {
  name                  = "icdc_nat_sg_${var.public_subnet_name}"
  vpc_id                = "${var.vpc_id}"

  ingress {
    from_port           = 0
    to_port             = 0
    protocol            = "-1"
    cidr_blocks         = "${var.private_subnet_cidr}"
  }

  ingress {
    from_port           = 22
    to_port             = 22
    protocol            = "tcp"

    cidr_blocks         = [
      "0.0.0.0/0",
    ]
  }

  ingress {
    from_port           = -1
    to_port             = -1
    protocol            = "icmp"

    cidr_blocks         = [
      "0.0.0.0/0",
    ]
  }

  egress {
    from_port           = 0
    to_port             = 0
    protocol            = "-1"

    cidr_blocks         = [
      "0.0.0.0/0",
    ]
  }

  tags {
    Name                = "icdc_nat_sg_${var.public_subnet_name}"
    Terraform = "true"
  }
}

resource "aws_instance" "nat_bastion" {
  ami                         = "${var.ami_id}"
  availability_zone           = "${var.public_subnet_az}"
  subnet_id                   = "${var.public_subnet_id}"
  instance_type               = "${var.nat_instance_type}"
  key_name                    = "${var.ssh_key_name}"
  user_data                   =  "${file("user.conf")}"
  connection {
    user = "${var.user}"
    private_key = "${file("../keys/icdc_devops")}"
  }
  provisioner "ansible" {
    plays {
      playbook = {
        file_path = "../playbook/nat.yml"
        roles_path = ["../roles"]
      }
      verbose = false
    }
    ansible_ssh_settings {
      insecure_no_strict_host_key_checking = "${var.insecure_no_strict_host_key_checking}"
      connect_timeout_seconds = 60
    }
  }
  vpc_security_group_ids = [
    "${aws_security_group.icdc_nat_sg.id}",
  ]
  associate_public_ip_address = true
  source_dest_check           = false

  tags = {
    Name                      = "nat_${var.public_subnet_name}"
  }
}
data "aws_eip" "icdc_nat_bastion" {
  tags = {
    Name                      = "icdc_nat_bastion"
  }
}
resource "aws_eip_association" "icdc_nat_bastion_eip" {
  instance_id                 = "${aws_instance.nat_bastion.id}"
  allocation_id               = "${data.aws_eip.icdc_nat_bastion.id}"
}

# locals {
#   route_id_count = "${length(data.aws_route_tables.route_table_ids.ids)}"
# }
resource "aws_route" "icdc_private_subnets_route" {
  count                     = "${length(var.private_subnet_route_ids)}"
  route_table_id            = "${var.private_subnet_route_ids[count.index]}"
  destination_cidr_block    = "0.0.0.0/0"
  instance_id               = "${aws_instance.nat_bastion.id}"
}
