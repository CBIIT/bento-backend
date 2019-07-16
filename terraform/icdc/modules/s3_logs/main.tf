#Let's create s3 bucket for the log
resource "aws_s3_bucket" "s3_bucket" {
  bucket        = "${var.bucket_name}"
  # acl           = "private"
  tags = {
    Name        = "${var.bucket_name}"
  }
}

# #Let's create folder 
# resource "aws_s3_bucket_object" "s3_bucket" {
#   bucket        = "${var.bucket_name}"
#   key           = "${var.log_path}"
#   source        = "/dev/null"
  
# }

data "aws_iam_policy_document" "s3_bucket_policy" {
  statement {
    sid       = "Allow LB to write logs"
    actions   = ["s3:PutObject"]
    resources = ["arn:aws:s3:::${aws_s3_bucket.s3_bucket.bucket}/*"]
    principals {
      type        = "AWS"
      identifiers = ["${var.account_arn}"]
    }
  }
}

resource "aws_s3_bucket_policy" "alb_logs" {
  bucket = "${aws_s3_bucket.s3_bucket.bucket}"
  policy = "${data.aws_iam_policy_document.s3_bucket_policy.json}"
}