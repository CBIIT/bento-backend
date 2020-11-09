resource "aws_api_gateway_vpc_link" "vpc_link" {
  name        = "${var.stack_name}-api-vpc-link"
  description = "public api gateway to private network loadbalancer"
  target_arns = var.target_arns
}

resource "aws_api_gateway_rest_api" "api" {
  name = var.api_gateway_name
  endpoint_configuration {
    types = var.endpoint_configuration
  }
}

resource "aws_api_gateway_resource" "api_resource" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "api"
}

resource "aws_api_gateway_method" "api_method" {
  rest_api_id      = aws_api_gateway_rest_api.api.id
  resource_id      = aws_api_gateway_resource.api_resource.id
  http_method      = "ANY"
  authorization    = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "api_intergration" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.api_resource.id
  http_method = aws_api_gateway_method.api_method.http_method

  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${var.nlb_dns_name}/"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.vpc_link.id
  timeout_milliseconds    = 29000

  cache_key_parameters = ["method.request.path.proxy"]
  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

resource "aws_api_gateway_method_response" "status_ok" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.api_resource.id
  http_method = aws_api_gateway_method.api_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_integration_response" "status_ok_intergration" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.api_resource.id
  http_method = aws_api_gateway_method.api_method.http_method
  status_code = aws_api_gateway_method_response.status_ok.status_code

//  response_templates = {
//    "application/json" = ""
//  }
}

resource "aws_api_gateway_domain_name" "domain" {
  domain_name = var.domain_name
  certificate_arn = var.certificate_arn
}

resource "aws_api_gateway_base_path_mapping" "base_path_map" {
  api_id      =  aws_api_gateway_rest_api.api.id
  domain_name = aws_api_gateway_domain_name.domain.domain_name
  stage_name = aws_api_gateway_deployment.deployment.stage_name
}

resource "aws_api_gateway_deployment" "deployment" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  stage_name  =  var.api_stage_name
}
