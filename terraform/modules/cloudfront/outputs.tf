output "cloudfront_distribution_endpoint" {
  value = aws_cloudfront_distribution.distribution.domain_name
}