# es security group

resource "aws_security_group" "es" {
  name = "${var.stack_name}-${terraform.workspace}-elasticsearch-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port = local.https_port
    to_port = local.https_port
    protocol = local.tcp_protocol
    cidr_blocks = var.subnet_ip_block
  }
}

resource "aws_security_group_rule" "all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.es.id
  type = "egress"
}