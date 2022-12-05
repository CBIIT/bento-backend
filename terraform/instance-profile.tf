resource "aws_iam_instance_profile" "integration_server" {
  count = var.create_instance_profile ? 1 : 0
  name = local.integration_server_profile_name
  role = aws_iam_role.integration_server[count.index].name
}

resource "aws_iam_role" "integration_server" {
  count = var.create_instance_profile ? 1 : 0
  name                 = local.integration_server_profile_name
  assume_role_policy   = data.aws_iam_policy_document.integration_server_assume_role.json
  permissions_boundary = terraform.workspace == "prod" ? null : local.permissions_boundary
}

resource "aws_iam_policy" "integration_server" {
  count = var.create_instance_profile ? 1 : 0
  name        = "${local.integration_server_profile_name}-policy"
  description = "IAM Policy for the integration server host in this account"
  policy      = data.aws_iam_policy_document.integration_server_policy.json
}

resource "aws_iam_role_policy_attachment" "integration_server" {
  count = var.create_instance_profile ? 1 : 0
  role       = aws_iam_role.integration_server[count.index].name
  policy_arn = aws_iam_policy.integration_server[count.index].arn
}

resource "aws_iam_role_policy_attachment" "managed_ecr" {
  for_each = var.create_instance_profile ? toset(local.managed_policy_arns) : toset([])
  role       = aws_iam_role.integration_server[0].name
  policy_arn = each.key
}
