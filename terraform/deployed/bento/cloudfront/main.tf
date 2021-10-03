resource "aws_s3_bucket" "files_bucket" {
  bucket = "${var.stack_name}-${terraform.workspace}-files"
  acl    = "private"
}

locals {
  s3_origin_id = "${var.stack_name}_files_origin_id"
}
# create origin access identity
resource "aws_cloudfront_origin_access_identity" "origin_access" {
  comment = "origin access for bento-files"
}

#create bucket for logs
resource "aws_s3_bucket" "access_logs" {
  bucket =  "${aws_s3_bucket.files_bucket.bucket}-cloudfront-logs"
  acl    = "private"
  tags = var.tags
}

#create s3 bucket policy
resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = aws_s3_bucket.files_bucket.bucket
  policy = data.aws_iam_policy_document.s3_policy.json
}

#create cloudfront distribution
resource "aws_cloudfront_distribution" "bento_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  price_class         = "PriceClass_100"

  web_acl_id = aws_wafv2_web_acl.waf.arn

  origin {
    domain_name = aws_s3_bucket.files_bucket.bucket_domain_name
    origin_id   = local.s3_origin_id
    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.origin_access.cloudfront_access_identity_path
    }
  }

  logging_config {
    include_cookies = false
    bucket          = aws_s3_bucket.access_logs.bucket_domain_name
    prefix          = "${var.stack_name}/cloudfront/logs"
  }


  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = local.s3_origin_id
    cache_policy_id = data.aws_cloudfront_cache_policy.managed_cache.id

    trusted_key_groups = [aws_cloudfront_key_group.key_group.id]
    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400

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

#create waf
resource "aws_wafv2_web_acl" "waf" {
  name        = "${var.stack_name}-ip-limiting-waf-rule"
  description = "This rule limit number of request per ip"
  scope       = "CLOUDFRONT"

  default_action {
    allow {}
  }

  rule {
    name     = "${var.stack_name}-${terraform.workspace}-ip-rate-rule"
    priority = 1

    action {
      block {}
    }

    statement {
      rate_based_statement {
        limit              = 100
        aggregate_key_type = "IP"
//        scope_down_statement {
//          regex_pattern_set_reference_statement {
//            arn = aws_wafv2_regex_pattern_set.api_files_pattern.arn
//            text_transformation {
//              priority = 1
//              type = "NONE"
//            }
//            field_to_match {
//              uri_path {}
//            }
//          }
//        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.stack_name}-${terraform.workspace}-ip-rate-metrics"
      sampled_requests_enabled   = true
    }
  }

  tags = var.tags

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.stack_name}-${terraform.workspace}files-request-ip"
    sampled_requests_enabled   = true
  }

}


resource "aws_wafv2_regex_pattern_set" "api_files_pattern" {
  name        =  "${var.stack_name}-${terraform.workspace}-api-files-pattern"
  scope       = "CLOUDFRONT"

  regular_expression {
    regex_string = "^/api/files/*"
  }
  tags = var.tags
}


#create public key
resource "aws_cloudfront_public_key" "public_key" {
  comment     = "bento files public key"
  encoded_key = file("${path.module}/cloudfront_public_key.pem")
  name        = "${var.stack_name}-${var.env}-pub-key"
}

resource "aws_cloudfront_key_group" "key_group" {
  comment = "bento files key group"
  items   = [aws_cloudfront_public_key.public_key.id]
  name    = "${var.stack_name}-${var.env}-key-group"
}

resource "aws_wafv2_web_acl_logging_configuration" "waf_logging" {
  log_destination_configs = [
    aws_kinesis_firehose_delivery_stream.firehose_stream.arn]
  resource_arn = aws_wafv2_web_acl.waf.arn
  redacted_fields {
    single_header {
      name = "user-agent"
    }
  }
  logging_filter {
    default_behavior = "DROP"

    filter {
      behavior = "KEEP"

      condition {
        action_condition {
          action = "BLOCK"
        }
      }
      requirement = "MEETS_ALL"
    }
  }
}

resource "aws_wafv2_ip_set" "ip_sets" {
  name               = "${var.stack_name}-${terraform.workspace}-ips-blocked-cloudfront"
  description        = "ips to blocked as result of violation of cloudfront waf rule"
  scope              = "CLOUDFRONT"
  ip_address_version = "IPV4"
  addresses          = ["127.0.0.1/32"]
  tags = var.tags
}