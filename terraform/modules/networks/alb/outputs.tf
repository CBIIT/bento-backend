output "alb_dns_name" {
  value = aws_lb.alb.dns_name
  description = "ALB dns name"
}
output "alb_security_group_id" {
  value = aws_security_group.alb-sg.id
}
output "alb_https_listener_arn" {
  description = "https listerner arn"
  value = aws_lb_listener.listener_https.arn
}
output "alb_zone_id" {
  description = "https listerner arn"
  value = aws_lb.alb.zone_id
}