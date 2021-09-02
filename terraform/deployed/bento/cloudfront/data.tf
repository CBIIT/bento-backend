data "aws_s3_bucket" "bento_files" {
  bucket = var.cloudfront_distribution_bucket_name
}

data "aws_iam_policy_document" "s3_policy" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${data.aws_s3_bucket.bento_files.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.origin_access.iam_arn]
    }
  }
}

#get managed cloudfront distribution
data "aws_cloudfront_cache_policy" "managed_cache" {
  name = "Managed-CachingOptimized"
}

data "aws_iam_policy_document" "kinesis_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    sid = ""
    principals {
      identifiers = ["firehose.amazonaws.com"]
      type = "Service"
    }
  }
}

data "aws_iam_policy_document" "firehose_policy" {
  statement {
    sid = ""
    effect = "Allow"
    actions = [
      "s3:AbortMultipartUpload",
      "s3:GetBucketLocation",
      "s3:GetObject",
      "s3:ListBucket",
      "s3:ListBucketMultipartUploads",
      "s3:PutObject"
    ]
    resources = [
      aws_s3_bucket.kinesis_log.arn,
      "${aws_s3_bucket.kinesis_log.arn}/*"
    ]
  }
  statement {
    effect = "Allow"
    sid = ""
    actions = ["iam:CreateServiceLinkedRole"]
    resources = ["arn:aws:iam::*:role/aws-service-role/wafv2.amazonaws.com/AWSServiceRoleForWAFV2Logging"]
  }
}
