output "nlb_dns_name" {
  value = aws_lb.alb.*.dns_name
  description = "ALB dns name"
}
output "nlb_tcp_listener_arn" {
  description = "nlb listerner arn"
  value = aws_lb_listener.tcp.arn
}
output "nlb_target_group_arn" {
  value = aws_lb_target_group.target.arn
}
output "nlb_ips" {
  value = flatten(data.aws_network_interface.nlb_ips.*.private_ips)
}