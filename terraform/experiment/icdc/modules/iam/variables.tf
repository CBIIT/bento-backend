variable "role_name" {
  default = "icdc-ec2-s3-access-role"
}
variable "s3_policy_name" {
  default = "icdc-s3-role-policy"
}
variable "ssm_policy_name" {
  default = "icdc-ssm-role-policy"
}
variable "attach_policy_s3" {
  default = "icdc-s3-iam-policy-attach"
}
variable "attach_policy_ssm" {
  default = "icdc-ssm-iam-policy-attach"
}
variable "profile_name" {
  default = "icdc-s3-role-profile"
}
