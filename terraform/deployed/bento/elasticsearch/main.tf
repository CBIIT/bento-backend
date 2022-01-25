terraform {
  required_version = ">= 0.12"
}

provider "aws" {
  profile = var.profile
  region = var.region
}
#set the backend for state file
terraform {
  backend "s3" {
    bucket = "bento-terraform-remote-state"
    key = "bento/elasticsearch/terraform.tfstate"
    workspace_key_prefix = "env"
    region = "us-east-1"
    encrypt = true
  }
}

locals {
  http_port = 80
  any_port = 0
  any_protocol = "-1"
  tcp_protocol = "tcp"
  https_port = "443"
  all_ips  = ["0.0.0.0/0"]
}

resource "aws_security_group" "es" {
  name = "${var.stack_name}-${terraform.workspace}-elasticsearch-sg"
  vpc_id = data.terraform_remote_state.network.outputs.vpc_id

  ingress {
    from_port = local.https_port
    to_port = local.https_port
    protocol = local.tcp_protocol
    cidr_blocks = [
      data.terraform_remote_state.network.outputs.vpc_cidr_block
    ]
  }
}
resource "aws_security_group_rule" "inbound_bastion" {
  from_port = local.https_port
  to_port = local.https_port
  protocol = local.tcp_protocol
  security_group_id = aws_security_group.es.id
  source_security_group_id = data.terraform_remote_state.bastion.outputs.bastion_security_group_id
  type = "ingress"
}

resource "aws_security_group_rule" "all_outbound" {
  from_port = local.any_port
  protocol = local.any_protocol
  to_port = local.any_port
  cidr_blocks = local.all_ips

  security_group_id = aws_security_group.es.id
  type = "egress"
}

resource "aws_iam_service_linked_role" "es" {
  count = var.create_es_service_role ? 1: 0
  aws_service_name = "es.amazonaws.com"
}

resource "aws_elasticsearch_domain" "es" {
  domain_name = "${var.stack_name}-${terraform.workspace}-elasticsearch"
  elasticsearch_version = var.elasticsearch_version

#  cluster_config {
#    instance_count = 1
#    instance_type = var.elasticsearch_instance_type
#    zone_awareness_enabled = true
#
#    zone_awareness_config {
#      availability_zone_count = 1
#    }
#  }

  vpc_options {
    subnet_ids = [
      data.terraform_remote_state.network.outputs.private_subnets_ids[0],
#      data.terraform_remote_state.network.outputs.private_subnets_ids[1]
    ]

    security_group_ids = [aws_security_group.es.id]
  }

  ebs_options {
    ebs_enabled = true
    volume_size = 120
  }

  access_policies = <<CONFIG
{
  "Version": "2012-10-17",
  "Statement": [
      {
          "Action": "es:*",
          "Principal": "*",
          "Effect": "Allow",
          "Resource": "arn:aws:es:${data.aws_region.region.name}:${data.aws_caller_identity.caller.account_id}:domain/${var.domain_name}/*"
      },
      {
          "Effect": "Allow",
          "Principal": {
            "AWS": "*"
          },
          "Action": [
            "es:ESHttp*"
          ],
          "Condition": {
            "IpAddress": {
              "aws:SourceIp": [
                "172.16.0.0/24"
              ]
            }
          },
        "Resource": "arn:aws:es:${data.aws_region.region.name}:${data.aws_caller_identity.caller.account_id}:domain/${var.domain_name}/*"
    }
  ]
}
  CONFIG
  snapshot_options {
    automated_snapshot_start_hour = 23
  }
  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.cloudwatch_log_group.arn
    log_type                 = "INDEX_SLOW_LOGS"
  }
  tags = var.tags
}


resource "aws_cloudwatch_log_group" "cloudwatch_log_group" {
  name = "${var.stack_name}-${terraform.workspace}-es-log-group"
}

resource "aws_cloudwatch_log_resource_policy" "cloudwatch_policy" {
  policy_name = "${var.stack_name}-${terraform.workspace}-es-log-policy"

  policy_document = <<CONFIG
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "es.amazonaws.com"
      },
      "Action": [
        "logs:PutLogEvents",
        "logs:PutLogEventsBatch",
        "logs:CreateLogStream"
      ],
      "Resource": "arn:aws:logs:*"
    }
  ]
}
CONFIG
}
