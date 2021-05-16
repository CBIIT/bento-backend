
data "aws_route53_zone" "zone" {
  name  = var.domain_name
}

resource "aws_route53_record" "www" {
  count =  var.env ==  "prod" ? 1 : 0
  name = "www"
  type = "CNAME"
  zone_id = data.aws_route53_zone.zone.zone_id
  ttl = "5"
  records = [var.domain_name]
}

resource "aws_route53_record" "prod_tier_records" {
  count =  var.env ==  "prod" ? 1 : 0
  name = var.domain_name
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}

resource "aws_route53_record" "lower_tiers_records" {
  count =  var.env ==  "prod" ? 0 : 1
  name = var.env
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}

resource "aws_route53_record" "api" {
  count =  var.env ==  "prod" ? 0:1
  name = "api-${var.stack_name}-${var.env}"
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}

resource "aws_route53_record" "api_prod" {
  count =  var.env ==  "prod" ? 1 : 0
  name = "api-${var.stack_name}"
  type = "A"
  zone_id = data.aws_route53_zone.zone.zone_id
  alias {
    evaluate_target_health = false
    name = module.alb.alb_dns_name
    zone_id = module.alb.alb_zone_id
  }
}