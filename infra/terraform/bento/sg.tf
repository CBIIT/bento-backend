#create alb security group
resource "aws_security_group" "alb-sg" {
  name   = "${var.stack_name}-${terraform.workspace}-alb-sg"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s-alb-sg", var.stack_name, terraform.workspace)
  },
  var.tags,
  )
}
resource "aws_security_group_rule" "inbound_http" {
  from_port   = local.http_port
  protocol    = local.tcp_protocol
  to_port     = local.http_port
  cidr_blocks = local.allowed_alb_ip_range
  security_group_id = aws_security_group.alb-sg.id
  type              = "ingress"
}
resource "aws_security_group_rule" "inbound_https" {
  from_port   = local.https_port
  protocol    = local.tcp_protocol
  to_port     = local.https_port
  cidr_blocks = local.allowed_alb_ip_range
  security_group_id = aws_security_group.alb-sg.id
  type              = "ingress"
}
resource "aws_security_group_rule" "all_outbound" {
  from_port   = local.any_port
  protocol    = local.any_protocol
  to_port     = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.alb-sg.id
  type              = "egress"
}
resource "aws_security_group" "fargate_sg" {
  name = "${var.stack_name}-${terraform.workspace}-fargate-sg"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s-fargate-sg",var.stack_name,terraform.workspace),
  },
  var.tags,
  )
}
resource "aws_security_group_rule" "all_outbound_fargate" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.fargate_sg.id
  type = "egress"
}
resource "aws_security_group_rule" "inbound_fargate" {
  for_each = toset(var.fargate_security_group_ports)
  from_port = each.key
  protocol = local.tcp_protocol
  to_port = each.key
  security_group_id = aws_security_group.fargate_sg.id
  cidr_blocks = [data.aws_vpc.vpc.cidr_block]
  type = "ingress"
}
resource "aws_security_group" "app_sg" {
  name = "${var.stack_name}-${terraform.workspace}-app-sg"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s-frontend-sg",var.stack_name,terraform.workspace),
  },
  var.tags,
  )
}
resource "aws_security_group_rule" "inbound_alb" {
  for_each = var.microservices
  from_port = each.value.port
  protocol = local.tcp_protocol
  to_port = each.value.port
  security_group_id = aws_security_group.app_sg.id
  source_security_group_id = aws_security_group.alb-sg.id
  type = "ingress"
}
resource "aws_security_group_rule" "all_outbound_app" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.app_sg.id
  type = "egress"
}

#security group for opensearch
resource "aws_security_group" "opensearch_sg" {
  count = var.create_opensearch_cluster
  name = "${var.stack_name}-${terraform.workspace}-opensearch-sg"
  vpc_id = var.vpc_id
  ingress {
    from_port = local.https_port
    to_port = local.https_port
    protocol = local.tcp_protocol
    cidr_blocks = var.opensearch_allowed_ip_block
  }
}

resource "aws_security_group_rule" "all_outbound_opensearch" {
  count = var.create_opensearch_cluster
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.opensearch_sg.id
  type = "egress"
}
