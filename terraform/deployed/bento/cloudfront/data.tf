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
