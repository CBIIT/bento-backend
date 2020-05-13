#get arn for iam_role
data "aws_iam_role" "cicd-role" {
  name = "ansible_ecs_role"
}
#create s3 bucket to host our site
resource "aws_s3_bucket" "s3-site" {
    bucket = join(".",[var.site,var.domain])

    website {
        index_document = var.index_document
    }

    logging {
        target_bucket = aws_s3_bucket.s3-site-log.id
    }
    versioning {
        enabled = true
    }
}

#create redirect to redirect http to https
resource "aws_s3_bucket" "redirect-http-https" {
    bucket = join(".",["www",var.site,var.domain])

    website {
        redirect_all_requests_to = join("",["https://",var.site,".",var.domain])
    }
}

#create s3 bucket to host website logs
resource "aws_s3_bucket" "s3-site-log" {
    bucket = join("-",[var.domain,"logs"])
    acl = "log-delivery-write"
}