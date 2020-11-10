
#set aws provider
provider "aws" {
  #change the region in the variable file. The default region is us-east-1
  region  = "${var.region}"
  #Profile comes from your aws credential. Check your $HOME/.aws/credential
  profile = "${var.profile}"
}

data "aws_caller_identity" "account_info" {}


# create an S3 bucket to store the state file. In this way we can share the state
resource "aws_s3_bucket" "terraform-state-s3-bucket" {
    bucket              = "${var.terraform-s3-bucket-name}"
    acl                 = "private"

    versioning {
      enabled           = true
    }
    logging {
    target_bucket = "${aws_s3_bucket.terraform-state-backend-logs.id}"
    target_prefix = "log/"
    }
    server_side_encryption_configuration {
      rule {
        apply_server_side_encryption_by_default {
          kms_master_key_id = "${var.kms_key_id}"
          sse_algorithm     = "${var.kms_key_id == "" ? "AES256" : "aws:kms"}"
        }
      }
    }
    lifecycle  {
      prevent_destroy   = false
    }
 
    # tags {
    #   Name              = "icdc_${var.environment}_terraform_remote_state_s3_bucket"
    #   ByTerraform       = "true"
    # }     
    
}

# create a dynamodb table, to enhance state sharing
resource "aws_dynamodb_table" "dynamodb-terraform-state-lock" {
  name = "icdc-${var.environment}-terraform-state-lock"
  hash_key = "LockID"
  read_capacity = 5
  write_capacity = 5
 
  attribute {
    name = "LockID"
    type = "S"
  }
 
  tags = {
    Name = "evay_${var.environment}_terraform_dynamo_state_lock"
    ByTerraform = true
  }
  lifecycle {
    prevent_destroy = false
  }
}

#create s3 policy docucment
data "aws_iam_policy_document" "terraform-state-bucket-policy-document" {
  statement {
    sid = "RequireEncryptedTransport"
    effect = "Deny"
    actions = [
      "s3:*",
    ]
    resources = [
      "${aws_s3_bucket.terraform-state-s3-bucket.arn}/*"
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
      "${aws_s3_bucket.terraform-state-s3-bucket.arn}/*"
    ]
    condition {
      test = "StringNotEquals"
      variable = "s3:x-amz-server-side-encryption"
      values = [
        "${var.kms_key_id == "" ? "AES256" : "aws:kms" }"
      ]
    }
    principals {
      type = "*"
      identifiers = ["*"]
    }
  }
}

#create bucket policy for the terraform state backend
resource "aws_s3_bucket_policy" "terraform-state-backend-policy" {
  bucket = "${aws_s3_bucket.terraform-state-s3-bucket.id}"
  policy = "${data.aws_iam_policy_document.terraform-state-bucket-policy-document.json}"
}

resource "aws_s3_bucket" "terraform-state-backend-logs" {
  bucket = "${var.terraform-s3-bucket-name}-logs"
  acl = "log-delivery-write"
  versioning {
    enabled = true
  }
 
  lifecycle  {
    prevent_destroy = false
  }
   tags = {
      Name = "evay_${var.environment}_terraform_logs"
      ByTerraform = "true"
  }
}
