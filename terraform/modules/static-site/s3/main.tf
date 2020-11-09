#create s3 bucket to host our site
resource "aws_s3_bucket" "s3_website" {
  bucket = join(".",[var.app,var.domain])
  acl = "public-read"
  force_destroy = true

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["HEAD", "GET",  "POST"]
    expose_headers = ["ETag"]
    max_age_seconds = "0"
    allowed_origins = ["*"]
  }
  website {
    index_document = var.index_document
  }

  logging {
    target_bucket = aws_s3_bucket.website_logs.id
  }
  versioning {
    enabled = true
  }
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.app,"website")
  },
  var.tags,
  )
}


#create a bucket to redirect http to https
resource "aws_s3_bucket" "redirect_http_https" {
  bucket = join(".",["www",var.app,var.domain])

  website {
    redirect_all_requests_to = join("",["https://",var.app,".",var.domain])
  }
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.app,"redirect-website")
  },
  var.tags,
  )
}

#create s3 bucket to host website logs
resource "aws_s3_bucket" "website_logs" {
  bucket = join("-",[var.domain,"logs"])
  acl = "log-delivery-write"
  tags = merge(
  {
    "Name" = format("%s-%s-%s",var.stack_name,var.app,"logs")
  },
  var.tags,
  )
}


#create policy document
data "aws_iam_policy_document" "public_read_policy" {
  statement {
    sid = "publicRead"
    actions = ["s3:GetObject"]
    resources = [ join("",[aws_s3_bucket.s3_website.arn,"/*"])]
    principals {
      type = "AWS"
      identifiers = ["*"]
    }
  }

}

#create policy document for redirect bucket
data "aws_iam_policy_document" "public_read_redirect_policy" {
  statement {
    sid = "publicReadRedirect"
    actions = ["s3:GetObject"]
    resources = [ join("",[aws_s3_bucket.redirect_http_https.arn,"/*"])]
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }
}
#create s3 bucket policy
resource "aws_s3_bucket_policy" "s3_public_read" {
  bucket = aws_s3_bucket.s3_website.id
  policy = data.aws_iam_policy_document.public_read_policy.json

}
resource "aws_s3_bucket_policy" "redirect_read_policy" {
  bucket = aws_s3_bucket.redirect_http_https.id
  policy = data.aws_iam_policy_document.public_read_redirect_policy.json
}