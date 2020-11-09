
resource "aws_iam_role" "s3_access_role" {
  name               = "${var.role_name}"
  assume_role_policy = "${file("../modules/iam/s3_assume_role_policy.json")}"
}
resource "aws_iam_policy" "s3_policy" {
  name        = "${var.s3_policy_name}"
  description = "icdc s3 bucket policy"
  policy      = "${file("../modules/iam/iam_s3_bucket_policy.json")}"
}
resource "aws_iam_policy" "ssm_policy" {
  name        = "${var.ssm_policy_name}"
  description = "icdc ssm policy"
  policy      = "${file("../modules/iam/iam_ssm_policy.json")}"
}
locals  {
  iam_policy_arn = ["${aws_iam_policy.s3_policy.arn}","${aws_iam_policy.ssm_policy.arn}"]
}
resource "aws_iam_policy_attachment" "s3_policy_role_attach" {
  name       = "${var.attach_policy_s3}"
  count      = 2
  roles      = ["${aws_iam_role.s3_access_role.name}"]
  policy_arn = "${local.iam_policy_arn[count.index]}"
}

resource "aws_iam_instance_profile" "s3_role_profile" {
  name  = "${var.profile_name}"
  role = "${aws_iam_role.s3_access_role.name}"
}

