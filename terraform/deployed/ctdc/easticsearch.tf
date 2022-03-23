resource "aws_elasticsearch_domain" "es" {
  domain_name = local.domain_name
  elasticsearch_version = var.elasticsearch_version
  vpc_options {
    subnet_ids = var.private_subnet_ids
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
          "Resource": "arn:aws:es:${data.aws_region.region.name}:${data.aws_caller_identity.caller.account_id}:domain/${local.domain_name}/*"
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
