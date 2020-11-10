//# create security group
//resource "aws_security_group" "neo4j_sg" {
//  name = "${var.stack_name}-neo4j-sg"
//  vpc_id = data.terraform_remote_state.network.outputs.vpc_id
//  tags = merge(
//  {
//    "Name" = format("%s-processor-sg",var.stack_name),
//  },
//  var.tags,
//  )
//}
//
//resource "aws_security_group_rule" "inbound_bastion" {
//  from_port = local.bastion_port
//  protocol = local.tcp_protocol
//  to_port = local.bastion_port
//  security_group_id = aws_security_group.frontend_sg.id
//  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
//  type = "ingress"
//}
//
//resource "aws_security_group_rule" "inbound_alb" {
//  from_port = var.container_port
//  protocol = local.tcp_protocol
//  to_port = var.container_port
//  security_group_id = aws_security_group.frontend_sg.id
//  source_security_group_id = data.terraform_remote_state.network.outputs.alb_security_group_id
//  type = "ingress"
//}
//
//resource "aws_security_group_rule" "all_outbound_frontend" {
//  from_port = local.any_port
//  protocol = local.any_protocol
//  to_port = local.any_port
//  cidr_blocks = local.all_ips
//
//  security_group_id = aws_security_group.frontend_sg.id
//  type = "egress"
//}
//
//
//#define inbound security group rule
//resource "aws_security_group_rule" "inbound_activemq_frontend" {
//  from_port = local.activemq_port
//  protocol = local.tcp_protocol
//  to_port = local.activemq_port
//  cidr_blocks = data.terraform_remote_state.network.outputs.private_subnets
//  security_group_id = aws_security_group.frontend_sg.id
//  type = "ingress"
//}
//
//
//resource "aws_security_group" "neo4j_security_group" {
//  name        = "${var.stack_name}-neo4j-sg"
//  description = "neo4j security group"
//  vpc_id      = data.terraform_remote_state.network.outputs.vpc_id
//
//  ingress {
//    from_port = 7474
//    to_port   = 7474
//    protocol  = "tcp"
//    cidr_blocks = [
//      "0.0.0.0/0",
//    ]
//  }
//
//  ingress {
//    from_port = 7473
//    to_port   = 7473
//    protocol  = "tcp"
//    cidr_blocks = [
//      "0.0.0.0/0",
//    ]
//  }
//
//  ingress {
//    from_port = 7687
//    to_port   = 7687
//    protocol  = "tcp"
//    cidr_blocks = [
//      "0.0.0.0/0",
//    ]
//  }
//
//  tags = {
//    Name        = "${var.stack_name}-neo4j-sg"
//    ByTerraform = "true"
//  }
//}
//
//resource "aws_instance" "neo4j" {
//  ami                    = data.terraform_remote_state.network.outputs.centos_ami
//  instance_type          = var.neo4j_instance_type
//  subnet_id              = data.terraform_remote_state.network.outputs.app_private_subnent_a_id
//  key_name               = data.terraform_remote_state.network.outputs.ssh_keypair
//  vpc_security_group_ids = [aws_security_group.base_security_group.id, aws_security_group.neo4j_security_group.id]
//  private_ip             = var.neo4j_private_ip
//
//  tags = {
//    Name        = "${var.stack_name}-neo4j"
//    Org         = var.org_name
//    ByTerraform = "true"
//  }
//}
//
//module "alb_neo4j_config" {
//  source       = "../modules/alb_svc_config"
//  stack        = var.stack_name
//  svc_name     = var.svc_neo4j
//  vpc_id       = data.terraform_remote_state.network.outputs.vpc_id
//  alb_arn      = aws_alb.alb.arn
//  domain_name  = var.domain
//  health_check = var.health_check_neo4j
//  priority     = var.rule_priority_neo4j
//  target_port  = "7474"
//  listener_arn = aws_lb_listener.alb_listener_https.arn
//}
//
//data "aws_lb_target_group" "neo4j" {
//  name       = "${var.stack_name}-${var.svc_neo4j}"
//  depends_on = [module.alb_neo4j_config]
//}
//
//resource "aws_lb_target_group_attachment" "alb_attach_neo4j" {
//  target_group_arn = data.aws_lb_target_group.neo4j.arn
//  target_id = aws_instance.neo4j.id
//  port      = "7474"
//}
//
