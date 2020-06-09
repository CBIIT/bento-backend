output "alb_arn" {
  value = aws_alb.alb.arn
}

output "alb_dns" {
  value = aws_alb.alb.dns_name
}

output "alb_zone_id" {
  value = aws_alb.alb.zone_id
}

