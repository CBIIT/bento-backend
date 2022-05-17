data "aws_iam_policy_document" "lambda_assume_policy" {
  statement {
    sid = ""
    effect = "Allow"
    actions = [
      "sts:AssumeRole"
    ]
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type = "Service"
    }
  }
}

data "aws_iam_policy_document" "lambda_exec_role_policy" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    principals {
      identifiers = ["events.amazonaws.com", "delivery.logs.amazonaws.com"]
      type        = "Service"
    }
    resources = [
      "arn:aws:logs:*:*:*"
    ]
  }
}