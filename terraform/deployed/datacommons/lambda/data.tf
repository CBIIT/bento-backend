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
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "arn:aws:logs:*:*:*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "ec2:DescribeInstances",
      "ec2:StartInstances",
      "ec2:StopInstances",
      "ec2:DescribeInstanceStatus"
    ]
    resources = ["*"]
  }
}