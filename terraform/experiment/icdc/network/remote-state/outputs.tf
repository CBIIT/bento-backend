#export the bucket name
output "terraform-remote-state-s3-bucket-name" {
  value = "${aws_s3_bucket.terraform-state-s3-bucket.bucket}"
}
output "terraform-dynamo-lock-table" {
  value = "${aws_dynamodb_table.dynamodb-terraform-state-lock.id}"
}
