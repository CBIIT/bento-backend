
resource "aws_iam_role" "ecs-instance-role" {
  name               = "${var.stack_name}-${var.env}-instance-role"
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
  name  = "${var.stack_name}-${var.env}-ecs-instance-profile"
  path  = "/"
  role = aws_iam_role.ecs-instance-role.id

}

resource "aws_iam_role" "ecs-service-role" {
  name               = "${var.stack_name}-${var.env}-ecs-service-role"
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
  name   = "${var.stack_name}-${var.env}-ssm-policy"
  path   = "/"
  policy = data.aws_iam_policy_document.ssm-policy-document.json
}

resource "aws_iam_policy_attachment" "ssm-policy-attachement" {
  name       = "${var.stack_name}-${var.env}-ssm-attach-policy"
  roles      = [aws_iam_role.ecs-instance-role.name]
  policy_arn = local.ssm_iam_policy_arn
}


data "aws_iam_policy_document" "ec2-policy-doc" {
  statement {
    effect = "Allow"
    actions = ["ec2:*"]
    resources = ["*"]
  }
}
resource "aws_iam_policy" "ec2-policy" {
  name   = "${var.stack_name}-${var.env}-ec2-policy"
  path   = "/"
  policy = data.aws_iam_policy_document.ec2-policy-doc.json
}
resource "aws_iam_policy_attachment" "ec2-policy-attach" {
  name  = "${var.stack_name}-ec2-attach-policy"
  policy_arn = aws_iam_policy.ec2-policy.arn
  roles = [aws_iam_role.ecs-instance-role.name]
}


data "aws_iam_policy_document" "ecs-policy-doc" {
  statement {
    effect = "Allow"
    actions = ["ecs:*"]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "ecr:*"
    ]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "ssm:*"
    ]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    actions = [
      "s3:*"
    ]
    resources = ["*"]
  }
}
resource "aws_iam_policy" "ecs-policy" {
  name   = "${var.stack_name}-${var.env}-ecs-policy"
  path   = "/"
  policy = data.aws_iam_policy_document.ecs-policy-doc.json
}
resource "aws_iam_policy_attachment" "ecs-policy-attach" {
  name  = "${var.stack_name}-${var.env}-ecs-policy-attachement"
  policy_arn = aws_iam_policy.ecs-policy.arn
  roles = [aws_iam_role.ecs-instance-role.name]
}