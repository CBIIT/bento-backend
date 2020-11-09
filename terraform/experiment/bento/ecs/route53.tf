data "aws_route53_zone" "zone" {
  name         = var.domain
}


 resource "aws_route53_record" "records" {
   name = var.stack_name
   type = "A"
   zone_id = data.aws_route53_zone.zone.zone_id
   alias {
     evaluate_target_health = false
     name = aws_alb.alb.dns_name
     zone_id = aws_alb.alb.zone_id
   }
 }

 resource "aws_route53_record" "api" {
   name = "api"
   type = "A"
   zone_id = data.aws_route53_zone.zone.zone_id
   alias {
     evaluate_target_health = false
     name = aws_alb.alb.dns_name
     zone_id = aws_alb.alb.zone_id
   }
 }
