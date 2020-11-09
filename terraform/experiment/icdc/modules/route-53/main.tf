#let's get our hosted zone information
data "aws_route53_zone" "zone" {
  name         = "${var.domain_name}."
}

#create route 53 alias to the loadbancer
resource "aws_route53_record" "alb_alias" {
  count     = "${length(var.hostnames)}"
  zone_id   = "${data.aws_route53_zone.zone.zone_id}"
  name      = "${element(var.hostnames,count.index)}"
  type      = "A"

  alias {
    name                   = "${var.dns_name}"
    zone_id                = "${var.alb_zone_id}"
    evaluate_target_health = "${var.evaluate_target_health}"
  }
}