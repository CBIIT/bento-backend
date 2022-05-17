module "lambda" {
  source = "../lambda"
  region = var.region
  stack_name = var.stack_name
  tags = var.tags
  functions = var.functions
}