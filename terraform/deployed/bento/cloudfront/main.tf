terraform {
  required_version = ">= 0.12"
}

provider "aws" {
  profile = var.profile
  region = var.region
}
#set the backend for state file
terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/ppdc/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}
locals {
  s3_origin_id = "bento_files_origin_id"
}
# create origin access identity
resource "aws_cloudfront_origin_access_identity" "origin_access" {
  comment = "origin access for bento-files"
}

#create bucket for logs
resource "aws_s3_bucket" "access_logs" {
  bucket =  "${data.aws_s3_bucket.bento_files.bucket}-cloudfront-logs"
  acl    = "private"
  tags = var.tags
}

#create s3 bucket policy
resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = data.aws_s3_bucket.bento_files.id
  policy = data.aws_iam_policy_document.s3_policy.json
}

#create cloudfront distribution
resource "aws_cloudfront_distribution" "bento_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  price_class         = "PriceClass_100"


  origin {
    domain_name = data.aws_s3_bucket.bento_files.bucket_domain_name
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

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
      headers = ["Origin"]
    }
    trusted_key_groups = [aws_cloudfront_key_group.key_group.id]
    viewer_protocol_policy = "allow-alll"
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

#create public key
resource "aws_cloudfront_public_key" "public_key" {
  comment     = "bento files public key"
  encoded_key = file("cloudfront_public_key.pem")
  name        = "bento-files-key"
}

resource "aws_cloudfront_key_group" "key_group" {
  comment = "bento files key group"
  items   = [aws_cloudfront_public_key.public_key.id]
  name    = "bento-files-key-group"
}
