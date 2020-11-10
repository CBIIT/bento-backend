output "s3_role_profile_name" {
  value = "${aws_iam_instance_profile.s3_role_profile.name}"
}
output "s3_role_profile_id" {
  value = "${aws_iam_instance_profile.s3_role_profile.id}"
}