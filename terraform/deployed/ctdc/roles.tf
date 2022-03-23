resource "aws_iam_service_linked_role" "es" {
  count = var.create_es_service_role ? 1: 0
  aws_service_name = "es.amazonaws.com"
}