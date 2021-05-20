locals {
  http_port = 80
  any_port = 0
  any_protocol = "-1"
  backend_port = 9200
  tcp_protocol = "tcp"
  https_port = "443"
  all_ips  = ["0.0.0.0/0"]
  ssh_user = var.ssh_user
  bastion_port = 22
  ssm_iam_policy_arn = aws_iam_policy.ssm-policy.arn
  my_account = format("arn:aws:iam::%s:root", data.aws_caller_identity.account.account_id)
}


