#create s3 bucket to store the logs
resource "aws_s3_bucket" "kinesis_log" {
  bucket = "${var.stack_name}-${terraform.workspace}-kenesis-firehose-steam"
  acl    = "private"
}
resource "aws_iam_role" "firehose_role" {
  name = "${var.stack_name}-${terraform.workspace}-firehose-role"
  assume_role_policy = data.aws_iam_policy_document.kinesis_assume_role_policy.json
}

resource "aws_iam_role_policy" "firehose_policy" {
  name = "${var.stack_name}-${terraform.workspace}-firehose-policy"
  policy = data.aws_iam_policy_document.firehose_policy.json
  role = aws_iam_role.firehose_role.id
}

resource "aws_kinesis_firehose_delivery_stream" "firehose_stream" {
  name        = "aws-waf-logs-${var.stack_name}-${terraform.workspace}-kenesis-firehose-steam"
  destination = "s3"

  s3_configuration {
    role_arn   = aws_iam_role.firehose_role.arn
    bucket_arn = aws_s3_bucket.kinesis_log.arn
  }
}
