
#choose ami
module "ami" {
  source = "../../../modules/cluster/ami"
}


#create security group
resource "aws_security_group" "bastion-sg" {
  name = "${var.stack_name}-bastion-sg"
  vpc_id = module.mgt-vpc.vpc_id
  tags = merge(
  {
    "Name" = format("%s-bastion-host-sg",var.stack_name),
  },
  var.tags,
  )
}

#create security group
resource "aws_security_group" "data-loader-sg" {
  name = "${var.stack_name}-data-loader-sg"
  vpc_id = module.mgt-vpc.vpc_id
  tags = merge(
  {
    "Name" = format("%s-data-loader-sg",var.stack_name),
  },
  var.tags,
  )
}

resource "aws_security_group_rule" "inbound_neo4j_bolt" {
  from_port = local.neo4j_bolt
  protocol = local.tcp_protocol
  to_port = local.neo4j_bolt
  cidr_blocks = var.mgt_private_subnets
  security_group_id = aws_security_group.data-loader-sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "inbound_neo4j_http" {
  from_port = local.neo4j_http
  protocol = local.tcp_protocol
  to_port = local.neo4j_http
  cidr_blocks = var.mgt_private_subnets
  security_group_id = aws_security_group.data-loader-sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "outbound_neo4j" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.data-loader-sg.id
  type = "egress"
}

#define inbound security group rule
resource "aws_security_group_rule" "inbound_bastion" {
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.bastion-sg.id
  type = "ingress"
}

#define outbound security group rule
resource "aws_security_group_rule" "outbound_bastion" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.bastion-sg.id
  type = "egress"
}

#provision bastion host
resource "aws_instance" "bastion_host" {
  ami            = module.ami.centos8_ami_id
  instance_type  = var.bastion_instance_type
  vpc_security_group_ids   = [aws_security_group.bastion-sg.id]
  key_name                 = var.ssh_key_name
  subnet_id                = module.mgt-vpc.public_subnets_ids[0]
  source_dest_check           = false
  iam_instance_profile =  aws_iam_instance_profile.ecs-instance-profile.name
  user_data  = data.template_cloudinit_config.user_data.rendered

  tags = merge(
  {
    "Name" = format("%s-bastion-host",var.stack_name),
  },
  var.tags,
  )
}

resource "aws_eip_association" "bastion_eip" {
  instance_id   = aws_instance.bastion_host.id
  allocation_id = data.aws_eip.bastion.id
}
