# resource "aws_route53_zone" "zone" {
#   name = var.domain
# }


# resource "aws_route53_record" "s3-site-records" {
#   zone_id = aws_route53_zone.zone.zone_id
#   name    = join(".",[var.site,var.domain])
#   type    = "A"

#   alias  {
#     name                   =   aws_cloudfront_distribution.site_distribution.domain_name
#     zone_id                =   aws_cloudfront_distribution.site_distribution.hosted_zone_id
#     evaluate_target_health = false
#   }
# }

