#create s3 bucket policy
resource "aws_s3_bucket_policy" "s3-public-read" {
    bucket = aws_s3_bucket.s3-site.id
    policy = data.aws_iam_policy_document.public-read-policy.json
}

#create policy document 
data "aws_iam_policy_document" "public-read-policy" {
  statement {
    sid = "publicRead"
    actions = ["s3:GetObject"]
    resources = [ join("",[aws_s3_bucket.s3-site.arn,"/*"])]
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }
}
resource "aws_s3_bucket_policy" "redirect_read_policy" {
  bucket = aws_s3_bucket.redirect-http-https.id
  policy = data.aws_iam_policy_document.public-read-policy-redirect.json
}

#create policy document for redirect bucket
data "aws_iam_policy_document" "public-read-policy-redirect" {
  statement {
    sid = "publicReadRedirect"
    actions = ["s3:GetObject"]
    resources = [ join("",[aws_s3_bucket.redirect-http-https.arn,"/*"])]
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }
}