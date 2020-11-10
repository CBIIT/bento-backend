//data "aws_route53_zone" "zone" {
//  name         = var.domain_name
//}
//
//resource "aws_route53_record" "records" {
//  name = data.aws_route53_zone.zone.name
//  type = "A"
//  zone_id = data.aws_route53_zone.zone.zone_id
//  alias {
//    evaluate_target_health = false
//    name = data.terraform_remote_state.network.outputs.alb_dns_name
//    zone_id = data.terraform_remote_state.network.outputs.alb_dns_zone_id
//  }
//}