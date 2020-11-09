output "alb_target_group_arn" {
  value = "${aws_alb_target_group.alb_target_group.*.arn}"
}
output "alb_listener_arn" {
  value = "${aws_lb_listener.alb_listener_https.*.arn}"
}
output "certificate_arn" {
  value = "${data.aws_acm_certificate.certificate.arn}"
}

