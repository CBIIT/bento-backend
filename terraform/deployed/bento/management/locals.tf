#define locals
locals {
  rdp = 3389
  bastion_port = 22
  neo4j_bolt = 7687
  neo4j_http = 7474
  any_port = 0
  docker_http = 2375
  docker_https = 2376
  ssh = 22
  http_port = 80
  any_protocol = "-1"
  tcp_protocol = "tcp"
  all_ips  = ["0.0.0.0/0"]
  ssm_iam_policy_arn = aws_iam_policy.ssm-policy.arn
}
