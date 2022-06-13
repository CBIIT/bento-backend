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
  count = var.create_opensearch_cluster ? 1: 0
  name = "${var.stack_name}-${terraform.workspace}-opensearch-sg"
  vpc_id = var.vpc_id
  ingress {
    from_port = local.https_port
    to_port = local.https_port
    protocol = local.tcp_protocol
    cidr_blocks = var.allowed_ip_blocks
  }
}

resource "aws_security_group_rule" "all_outbound_opensearch" {
  count = var.create_opensearch_cluster ? 1: 0
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.opensearch_sg[count.index].id
  type = "egress"
}

#create database security group
resource "aws_security_group" "database-sg" {
  count = var.create_db_instance ? 1 : 0
  name = "${var.stack_name}-${terraform.workspace}-database-sg"
  description = "${var.stack_name} database security group"
  vpc_id = var.vpc_id
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"database-sg")
  },
  var.tags,
  )
}
resource "aws_security_group_rule" "neo4j_http" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_http
  protocol = local.tcp_protocol
  to_port = local.neo4j_http
  cidr_blocks = var.allowed_ip_blocks
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}
resource "aws_security_group_rule" "bastion_host_ssh" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.bastion_port
  protocol = local.tcp_protocol
  to_port = local.bastion_port
  source_security_group_id = var.bastion_host_security_group_id
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}
resource "aws_security_group_rule" "neo4j_https" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_https
  protocol = local.tcp_protocol
  to_port = local.neo4j_https
  cidr_blocks = var.allowed_ip_blocks
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}

resource "aws_security_group_rule" "neo4j_bolt" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_bolt
  protocol = local.tcp_protocol
  to_port = local.neo4j_bolt
  cidr_blocks = var.allowed_ip_blocks
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound_db" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "egress"
}

//Dataloader security rules
resource "aws_security_group_rule" "dataloader_http" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_http
  protocol = local.tcp_protocol
  to_port = local.neo4j_http
  source_security_group_id = var.bastion_host_security_group_id
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}
resource "aws_security_group_rule" "dataloader_bolt" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_bolt
  protocol = local.tcp_protocol
  to_port = local.neo4j_bolt
  source_security_group_id = var.bastion_host_security_group_id
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}
resource "aws_security_group_rule" "katalon_bolt" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_bolt
  protocol = local.tcp_protocol
  to_port = local.neo4j_bolt
  source_security_group_id = var.katalon_security_group_id
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}
resource "aws_security_group_rule" "katalon_http" {
  count = var.create_db_instance ? 1 : 0
  from_port = local.neo4j_http
  protocol = local.tcp_protocol
  to_port = local.neo4j_http
  source_security_group_id = var.katalon_security_group_id
  security_group_id = aws_security_group.database-sg[count.index].id
  type = "ingress"
}