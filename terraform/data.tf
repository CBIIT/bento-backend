data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
data "aws_vpc" "vpc" {
  id = var.vpc_id
}
data "aws_iam_policy_document" "s3_alb_policy" {
  statement {
    sid = "allowalbaccount"
    effect = "Allow"
    principals {
      identifiers = ["arn:aws:iam::${lookup(var.aws_account_id,var.region,"us-east-1" )}:root"]
      type        = "AWS"
    }
    actions = ["s3:PutObject"]
    resources = ["arn:aws:s3:::${module.s3.bucket_name}/*"]
  }
  statement {
    sid = "allowalblogdelivery"
    effect = "Allow"
    principals {
      identifiers = ["delivery.logs.amazonaws.com"]
      type        = "Service"
    }
    actions = ["s3:PutObject"]
    resources = ["arn:aws:s3:::${module.s3.bucket_name}/*"]
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
    resources = ["arn:aws:s3:::${module.s3.bucket_name}"]
    principals {
      identifiers = ["delivery.logs.amazonaws.com"]
      type        = "Service"
    }
  }
}

data "aws_acm_certificate" "amazon_issued" {
  domain      =  var.certificate_domain_name
  types       = [local.cert_types]
  most_recent = true
}

data "aws_iam_policy_document" "integration_server_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "ec2.amazonaws.com",
        "opensearchservice.amazonaws.com"
        ]
    }
  }
}


data "aws_iam_policy_document" "integration_server_policy" {

  statement {
    effect    = "Allow"
    actions   = ["iam:GetRole"]
    resources = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["iam:PassRole"]
    resources = [
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/power-user-*",
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/ccdc-*"
      ]
  }
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetResourcePolicy",
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecretVersionIds"
    ]
    resources = [
      "arn:aws:secretsmanager:*:${data.aws_caller_identity.current.account_id}:secret:*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetRandomPassword",
      "secretsmanager:ListSecrets"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:Encrypt",
      "kms:ListKeys"
    ]
    resources = ["arn:aws:kms:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:key/*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "ecs:ListTasks",
      "ecs:ListTaskDefinitions",
      "ecs:ListServices",
      "ecs:ListClusters",
      "ecs:ListServices",
      "ecs:ListTaskDefinitionFamilies",
      "ecs:DescribeTaskDefinitions",
      "ecs:DeregisterTaskDefinition",
      "ecs:DiscoverPollEndpoint",
      "ecs:RegisterTaskDefinition",
      "ecs:CreateTaskSet",
      "ecs:DescribeTaskDefinition"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "ecs:CreateService",
      "ecs:ListTasks",
      "ecs:DeleteService",
      "ecs:ListTagsForResource",
      "ecs:ListContainerInstances",
      "ecs:DescribeTasks",
      "ecs:ListAttributes",
      "ecs:DescribeServices",
      "ecs:DescribeTaskSets",
      "ecs:DescribeContainerInstances",
      "ecs:DeleteAttributes",
      "ecs:DescribeClusters",
      "ecs:DeleteTaskSet",
      "ecs:DeregisterContainerInstance",
      "ecs:ExecuteCommand",
      "ecs:Poll",
      "ecs:PutAttributes",
      "ecs:RegisterContainerInstance",
      "ecs:RunTask",
      "ecs:StartTask",
      "ecs:StartTelemetrySession",
      "ecs:StopTask",
      "ecs:SubmitContainerStateChange",
      "ecs:SubmitTaskStateChange",
      "ecs:UpdateCluster",
      "ecs:UpdateClusterSettings",
      "ecs:UpdateContainerAgent",
      "ecs:UpdateContainerInstancesState",
      "ecs:UpdateService",
      "ecs:UpdateServicePrimaryTaskSet",
      "ecs:UpdateTaskSet",
      "ecs:TagResource",
      "ecs:UntagResource"
    ]
    resources = [
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:cluster/*",
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:container-instance/*/*",
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:service/*/*",
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:task/*/*",
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:task-definition/*:*",
      "arn:aws:ecs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:task-set/*/*/*"
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetRepositoryScanningConfiguration",
      "ecr:BatchImportUpstreamImage",
      "ecr:CompleteLayerUpload",
      "ecr:DescribeImageReplicationStatus",
      "ecr:DescribeImages",
      "ecr:DescribeRegistry",
      "ecr:DescribeRepositories",
      "ecr:GetAuthorizationToken",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:ListImages",
      "ecr:ListTagsForResource",
      "ecr:PutImage",
      "ecr:PutImageTagMutability",
      "ecr:PutReplicationConfiguration",
      "ecr:ReplicateImage",
      "ecr:StartImageScan",
      "ecr:TagResource",
      "ecr:UntagResource",
      "ecr:UploadLayerPart"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:GetObjectAttributes",
      "s3:GetObjectVersion",
      "s3:ListAllMyBuckets",
      "s3:ListBucket",
      "s3:ListBucketVersions",
      "s3:PutObject",
      "s3:DeleteObject"
    ]
    resources = ["*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:*"]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "iam:PassRole",
      "iam:GetRole",
    ]
    resources = [
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.stack_name}-${terraform.workspace}-task*",
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/power-user-${var.stack_name}-${terraform.workspace}-task*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "es:ESHttpDelete",
      "es:ESHttpGet",
      "es:ESHttpHead",
      "es:ESHttpPatch",
      "es:ESHttpPost",
      "es:ESHttpPut"
    ]
    resources = [
      "arn:aws:es:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:domain/*",
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "ec2:DescribeInstances",
      "ec2:DescribeImages",
      "ec2:DescribeTags",
      "ec2:DescribeSnapshots"
    ]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "rds:AddRoleToDBCluster",
      "rds:AddRoleToDBInstance",
      "rds:CopyDBClusterSnapshot",
      "rds:CopyDBSnapshot",
      "rds:CreateDBSnapshot",
      "rds:DescribeDBClusters",
      "rds:DescribeDBEngineVersions",
      "rds:DescribeDBInstances",
      "rds:DownloadCompleteDBLogFile",
      "rds:ModifyDBInstance",
      "rds:RebootDBInstance",
      "rds:RestoreDBClusterFromSnapshot",
      "rds:RestoreDBInstanceFromDBSnapshot",
      "rds:RestoreDBInstanceFromS3",
      "rds:StartDBCluster",
      "rds:StartDBInstance",
      "rds:StopDBCluster",
      "rds:StopDBInstance"
    ]
    resources = ["arn:aws:rds:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:db:ccdc-*"]
  }
}