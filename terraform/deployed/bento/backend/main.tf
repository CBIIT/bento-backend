provider "aws" {
  region = var.region
  profile = var.profile
}

resource "aws_dynamodb_table" "state_lock" {
  hash_key = "LockID"
  write_capacity = 10
  read_capacity = 10
  name = "${var.stack_name}-terraform-state-key-lock"
  attribute {
    name = "LockID"
    type = "S"
  }
  lifecycle {
    prevent_destroy = false
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"state-dynamodb-key-lock")
  },
  var.tags,
  )
}

resource "aws_s3_bucket" "state" {
  bucket = "${var.stack_name}-terraform-remote-state"
  acl = "private"
  lifecycle {
    prevent_destroy = false
  }
  versioning {
    enabled = true
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = var.kms_key_id == ""? "AES256" : "aws:kms"
        kms_master_key_id = var.kms_key_id
      }
    }
  }
  tags = merge(
  {
    "Name" = format("%s-%s",var.stack_name,"state-bucket")
  },
  var.tags,
  )
}


#create s3 terraform state bucket policy docucment
data "aws_iam_policy_document" "state-bucket-policy-document" {
  statement {
    sid = "RequireEncryptedTransport"
    effect = "Deny"
    actions = [
      "s3:*",
    ]
    resources = [
      "${aws_s3_bucket.state.arn}/*"
    ]
    condition {
      test = "Bool"
      variable = "aws:SecureTransport"
      values = [
        false,
      ]
    }
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }

  statement {
    sid = "RequireEncryptedStorage"
    effect = "Deny"
    actions = [
      "s3:PutObject",
    ]
    resources = [
      "${aws_s3_bucket.state.arn}/*"
    ]
    condition {
      test = "StringNotEquals"
      variable = "s3:x-amz-server-side-encryption"
      values = [
        var.kms_key_id == "" ? "AES256" : "aws:kms"
      ]
    }
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }
}

#create bucket policy for the terraform state backend
resource "aws_s3_bucket_policy" "terraform-state-policy" {
  bucket = aws_s3_bucket.state.id
  policy = data.aws_iam_policy_document.state-bucket-policy-document.json
}
