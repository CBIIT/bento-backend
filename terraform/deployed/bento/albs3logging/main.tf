locals {
  alb_s3_bucket_name = "${var.stack_name}-alb-access-logs"
}


resource "aws_s3_bucket" "alb_logs_bucket" {
  bucket = local.alb_s3_bucket_name
  acl = "private"
  policy = data.aws_iam_policy_document.s3_policy.json
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
  lifecycle_rule {
    id = "transition_to_standard_ia"
    enabled = (var.s3_object_expiration_days - var.s3_object_standard_ia_transition_days > 30)
    transition {
      storage_class = "STANDARD_IA"
      days = var.s3_object_standard_ia_transition_days
    }
    noncurrent_version_transition {
      days = var.s3_object_nonactive_expiration_days - 30 > 30 ? 30 : var.s3_object_nonactive_expiration_days + 30
      storage_class = "STANDARD_IA"
    }
  }
  lifecycle_rule {
    id = "expire_objects"
    enabled = true
    expiration {
      days = var.s3_object_expiration_days
    }
    noncurrent_version_expiration {
      days = var.s3_object_nonactive_expiration_days
    }
  }
  tags = var.tags
}
