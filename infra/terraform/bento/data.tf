data "aws_caller_identity" "current" {}
data "aws_iam_policy_document" "s3_policy" {
  statement {
    sid = "allowalbaccount"
    effect = "Allow"
    principals {
      identifiers = ["arn:aws:iam::${lookup(var.aws_account_id,var.region,"us-east-1" )}:root"]
      type        = "AWS"
    }
    actions = ["s3:PutObject"]
    resources = ["arn:aws:s3:::${local.alb_log_bucket_name}/*"]
  }
  statement {
    sid = "allowalblogdelivery"
    effect = "Allow"
    principals {
      identifiers = ["delivery.logs.amazonaws.com"]
      type        = "Service"
    }
    actions = ["s3:PutObject"]
    resources = ["arn:aws:s3:::${local.alb_log_bucket_name}/*"]
    condition {
      test     = "StringEquals"
      values   = ["bucket-owner-full-control"]
      variable = "s3:x-amz-acl"
    }
  }
  statement {
    sid = "awslogdeliveryacl"
    effect = "Allow"
    actions = ["s3:GetBucketAcl"]
    resources = ["arn:aws:s3:::${local.alb_log_bucket_name}"]
    principals {
      identifiers = ["delivery.logs.amazonaws.com"]
      type        = "Service"
    }
  }
}

data "aws_iam_policy_document" "task_execution_policy_document" {
    statement {
        effect = "Allow"
        actions = [
            "logs:CreateLogGroup",
        ]
        resources = [
            "arn:aws:logs:*:*:*"
        ]
    }
    statement {
        effect = "Allow"
        actions = [
            "ecs:RunTask"
        ]
        resources = ["*"]
    }
    statement {
      effect = "Allow"
      actions = [
          "iam:PassRole"
      ]
      condition {
        test     = "StringLike"
        values   = ["ecs-tasks.amazonaws.com"]
        variable = "iam:PassedToService"
      }
    }
    statement {
        effect = "Allow"
        actions = [
            "kms:Decrypt",
            "secretsmanager:GetSecretValue"
        ]
        resources = ["*"]
    }
}