resource "aws_iam_role" "ecs-instance-role" {
  name               = var.ecs_role_name
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.ecs-instance-policy.json
}

data "aws_iam_policy_document" "ecs-instance-policy" {
    statement {
        actions = ["sts:AssumeRole"]

        principals {
            type        = "Service"
            identifiers = ["ec2.amazonaws.com"]
        }
    }
}

resource "aws_iam_role_policy_attachment" "ecs-instance-role-attachment" {
  role       = aws_iam_role.ecs-instance-role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource "aws_iam_instance_profile" "ecs-instance-profile" {
  name  = "ecs-instance-profile"
  path  = "/"
  role = aws_iam_role.ecs-instance-role.id
  # provisioner "local-exec" {
  #   command = "sleep 10"
  # }
}

resource "aws_iam_role" "ecs-service-role" {
  name               = "ecs-service-role"
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.ecs-service-policy.json
}

resource "aws_iam_role_policy_attachment" "ecs-service-role-attachment" {
  role       = aws_iam_role.ecs-service-role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceRole"
}

data "aws_iam_policy_document" "ecs-service-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "ssm-policy-document" {
  statement {
    sid = "10"

    effect = "Allow"
    actions = [
      "cloudwatch:PutMetricData",
      "ds:CreateComputer",
      "ds:DescribeDirectories",
      "ec2:DescribeInstanceStatus",
      "logs:*",
      "ssm:*",
      "ec2messages:*"
    ]
    resources = [
      "*"
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "iam:CreateServiceLinkedRole"
    ]

    resources = [
      "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*"
    ]

    condition {
      test     = "StringLike"
      variable = "iam:AWSServiceName"
      values = [
       "ssm.amazonaws.com"
      ]
    }
  }

  statement {
    effect = "Allow"
    actions = [
      "iam:DeleteServiceLinkedRole",
      "iam:GetServiceLinkedRoleDeletionStatus"
    ]

    resources = [
      "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*",
    ]
  }
}

resource "aws_iam_policy" "ssm-policy" {
  name   = "ssm-policy"
  path   = "/"
  policy = data.aws_iam_policy_document.ssm-policy-document.json
}


locals  {
  ssm_iam_policy_arn = aws_iam_policy.ssm-policy.arn
}

resource "aws_iam_policy_attachment" "ssm-policy-attachement" {
  name       = "ssm-attach-policy"
  roles      = [aws_iam_role.ecs-instance-role.name]
  policy_arn = local.ssm_iam_policy_arn
}

data "aws_iam_policy_document" "s3-policy-document" {
    statement {
        effect = "Allow"
        actions = ["s3:*"]
        resources = ["*"]
    }
}
resource "aws_iam_policy" "s3-policy" {
  name   = "ecs-s3-policy"
  path   = "/"
  policy = data.aws_iam_policy_document.s3-policy-document.json
}
locals  {
  s3_iam_policy_arn = aws_iam_policy.s3-policy.arn
}
resource "aws_iam_policy_attachment" "s3-policy-attachement" {
  name       = "s3-attach-policy"
  roles      = [aws_iam_role.ecs-instance-role.name]
  policy_arn = local.s3_iam_policy_arn
}
