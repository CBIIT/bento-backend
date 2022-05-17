module "lambda" {
  for_each = var.functions
  source = "../lambda"
  region = var.region
  stack_name = var.stack_name
  tags = var.tags
  functions = var.functions
}