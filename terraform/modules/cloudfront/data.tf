#get managed s3cors policy
data "aws_cloudfront_origin_request_policy" "s3_cors" {
  id = "88a5eaf4-2fd4-4709-b370-b4c650ea3fcf"
}

#get managed cloudfront distribution
data "aws_cloudfront_cache_policy" "managed_cache" {
  name = "Managed-CachingOptimized"
}
data "aws_s3_bucket" "cloudfront_s3_bucket" {
  bucket = var.cloudfront_distribution_bucket_name
}

data "aws_s3_bucket" "cloudfront_log_bucket" {
  bucket = var.cloudfront_distribution_log_bucket_name
}

data "aws_iam_policy_document" "s3_policy" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${data.aws_s3_bucket.cloudfront_s3_bucket.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.origin_access.iam_arn]
    }
  }
}