
locals {
  vault_password = "icdcJenkins2**0"
  devops_user    = "ecs-user"
  devops_ssh_key_pub = file("icdc_devops.pub")
  activemq_port = 61613
  bastion_port = 22
  any_port = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
}
