locals {
  http_port = 80
  any_port = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  https_port = "443"
  all_ips = ["0.0.0.0/0"]
  domain_name = "${var.stack_name}-${terraform.workspace}-elasticsearch"
}


