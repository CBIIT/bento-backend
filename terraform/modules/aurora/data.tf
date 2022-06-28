// IAM Role + Policy attach for Enhanced Monitoring
data "aws_iam_policy_document" "assumed_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["monitoring.rds.amazonaws.com"]
    }
  }
}
data "aws_vpc" "vpc" {
  id = var.vpc_id
}