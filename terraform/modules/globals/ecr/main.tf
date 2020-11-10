
locals {
  my_account = "${format("arn:aws:iam::%s:root", data.aws_caller_identity.account.account_id)}"
}

data "aws_caller_identity" "account" {
}

resource "aws_ecr_repository" "ecr" {
  name = var.stack_name
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"ecr-registry")
  },
  var.tags,
  )
}

resource "aws_ecr_repository_policy" "ecr_policy" {
  repository = aws_ecr_repository.ecr.name
  policy     = data.aws_iam_policy_document.ecr_policy_doc.json
}

data "aws_iam_policy_document" "ecr_policy_doc" {

  statement {
    sid    = "ElasticContainerRegistryPushAndPull"
    effect = "Allow"

    principals {
      identifiers = [local.my_account]
      type        = "AWS"
    }
    actions = [
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability",
      "ecr:PutImage",
      "ecr:InitiateLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:CompleteLayerUpload",
    ]
  }
}


