
#provision bastion host
resource "aws_instance" "katalon_host" {
  ami            = data.aws_ami.windows_ami.id
  instance_type  = "t3.medium"
  vpc_security_group_ids   = [aws_security_group.katalon-sg.id]
  key_name                 = var.ssh_key_name
  subnet_id                = module.mgt-vpc.public_subnets_ids[0]
  source_dest_check           = false
//  iam_instance_profile =  aws_iam_instance_profile.ecs-instance-profile.name
//  user_data  = data.template_cloudinit_config.user_data.rendered

  tags = merge(
  {
    "Name" = format("%s-windows-host",var.stack_name),
  },
  var.tags,
  )
}

#create security group
resource "aws_security_group" "katalon-sg" {
  name = "${var.stack_name}-katalon-sg"
  vpc_id = module.mgt-vpc.vpc_id
  tags = merge(
  {
    "Name" = format("%s-katalon-host-sg",var.stack_name),
  },
  var.tags,
  )
}

//resource "aws_security_group_rule" "katalon_inbound_neo4j_bolt" {
//  from_port = local.neo4j_bolt
//  protocol = local.tcp_protocol
//  to_port = local.neo4j_bolt
//  cidr_blocks = var.mgt_private_subnets
//  security_group_id = aws_security_group.data-loader-sg.id
//  type = "ingress"
//}
//resource "aws_security_group_rule" "katalon_inbound_neo4j_http" {
//  from_port = local.neo4j_http
//  protocol = local.tcp_protocol
//  to_port = local.neo4j_http
//  cidr_blocks = var.mgt_private_subnets
//  security_group_id = aws_security_group.data-loader-sg.id
//  type = "ingress"
//}
//
#define inbound security group rule
resource "aws_security_group_rule" "inbound_rdp" {
  from_port = local.rdp
  protocol = local.tcp_protocol
  to_port = local.rdp
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.katalon-sg.id
  type = "ingress"
}

#define outbound security group rule
resource "aws_security_group_rule" "outbound_katalon" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.katalon-sg.id
  type = "egress"
}
resource "aws_eip_association" "katalon-test-eip" {
  instance_id   = aws_instance.katalon_host.id
  allocation_id = data.aws_eip.rdp.id
}