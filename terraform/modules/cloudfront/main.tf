locals {
  s3_origin_id = "${var.stack_name}-${var.env}-s3-origin-id"
}

#create s3 bucket policy
resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = data.aws_s3_bucket.cloudfront_s3_bucket.bucket
  policy = data.aws_iam_policy_document.s3_policy.json
}

resource "aws_s3_bucket_cors_configuration" "cors" {
  bucket = data.aws_s3_bucket.cloudfront_s3_bucket.bucket

  cors_rule {
    allowed_headers = [
      "Authorization",
      "Content-Range",
      "Accept",
      "Content-Type",
      "Origin",
      "Range",
      "Access-Control-Allow-Origin"
    ]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = [
       "*.cancer.gov",
       "*.cloudfront.net",
       "github.com",
       "raw.githubusercontent.com",
    ]
    expose_headers  = [
      "Content-Range",
      "Content-Length",
      "ETag"
    ]
    max_age_seconds = 3000
  }

}

# create origin access identity
resource "aws_cloudfront_origin_access_identity" "origin_access" {
  comment =  var.cloudfront_origin_access_identity_description
}

#create cloudfront distribution
resource "aws_cloudfront_distribution" "distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  price_class         = "PriceClass_100"

  origin {
    domain_name = data.aws_s3_bucket.cloudfront_s3_bucket.bucket_domain_name
    origin_id   = local.s3_origin_id
    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.origin_access.cloudfront_access_identity_path
    }
  }

  logging_config {
    include_cookies = false
    bucket          = data.aws_s3_bucket.cloudfront_log_bucket.bucket_domain_name
    prefix          = var.cloudfront_log_path_prefix_key
  }
  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD","OPTIONS"]
    cached_methods   = ["GET", "HEAD","OPTIONS"]
    target_origin_id = local.s3_origin_id
    cache_policy_id = data.aws_cloudfront_cache_policy.managed_cache.id
    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400

//    forwarded_values {
//      query_string = false
//      cookies {
//        forward = "none"
//      }
//    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
    tags = var.tags
}