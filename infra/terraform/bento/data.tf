data "aws_caller_identity" "current" {}
data "aws_vpc" "vpc" {
  id = var.vpc_id
}
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
            "kms:GetPublicKey",
            "kms:GenerateDataKey",
            "kms:DescribeKey"
        ]
        resources = ["*"]
    }
}
data "aws_iam_policy_document" "ecs_task_policy" {
  statement {
    effect = "Allow"
    actions = [
      "es:ESHttpGet",
      "s3:ListBucket",
      "es:ESHttpDelete",
      "s3:GetBucketAcl",
      "es:ESCrossClusterGet",
      "s3:PutObject",
      "s3:GetObject",
      "es:ESHttpHead",
      "es:ESHttpPost",
      "es:ESHttpPatch",
      "s3:GetObjectVersionAcl",
      "s3:GetBucketLocation",
      "es:ESHttpPut",
      "s3:GetObjectVersion"
    ]
    resources = ["*"]
  }
}