#fetch essential-dev cets
data "aws_acm_certificate" "certificate" {
  domain = join(".",["*",var.domain])
  types  = ["AMAZON_ISSUED"]
}