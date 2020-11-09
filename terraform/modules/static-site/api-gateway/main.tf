resource "aws_api_gateway_rest_api" "api" {
  name        = var.api_gateway_name
  description = "api gateway for bento frame work"
}

resource "aws_api_gateway_resource" "bento_resource" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "icdc"
}

resource "aws_api_gateway_resource" "api_resource" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "api"
}
resource "aws_api_gateway_method" "bento_method" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.bento_resource.id
  http_method   = "ANY"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }

}
resource "aws_api_gateway_method" "api_method" {
  authorization = "NONE"
  http_method = "ANY"
  resource_id = aws_api_gateway_resource.api_resource.id
  rest_api_id = aws_api_gateway_rest_api.api.id

  request_parameters = {
    "method.request.path.proxy" = true
  }

}

resource "aws_api_gateway_integration" "bento_integration" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.bento_resource.id
  http_method   = aws_api_gateway_method.bento_method.http_method
  type          = "HTTP_PROXY"
  integration_http_method = "ANY"

  uri =  "http://bento.essential-dev.com.s3-website-us-east-1.amazonaws.com/"

  passthrough_behavior = "WHEN_NO_MATCH"
  request_parameters =  {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }

}


resource "aws_api_gateway_method_response" "status_ok_bento" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.bento_resource.id
  http_method = aws_api_gateway_method.bento_method.http_method
  status_code = "200"

}

resource "aws_api_gateway_method_response" "status_ok_resource" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.api_resource.id
  http_method = aws_api_gateway_method.api_method.http_method
  status_code = "200"

}

resource "aws_api_gateway_integration" "proxy_integration" {
  rest_api_id          = aws_api_gateway_rest_api.api.id
  resource_id          = aws_api_gateway_resource.api_resource.id
  http_method          = aws_api_gateway_method.api_method.http_method
  type                 = "HTTP_PROXY"
  uri                  = "http://${join("",var.alb_dns_name)}/api"
  //uri =  "http://bento.essential-dev.com.s3-website-us-east-1.amazonaws.com/{proxy}"
  integration_http_method = "ANY"
  passthrough_behavior = "WHEN_NO_MATCH"

  cache_key_parameters = ["method.request.path.proxy"]
  request_parameters =  {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }

}

resource "aws_api_gateway_integration_response" "status_ok_integration_bento" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.bento_resource.id
  http_method = aws_api_gateway_method.bento_method.http_method
  status_code = aws_api_gateway_method_response.status_ok_bento.status_code

//  response_templates = {
//    "application/json" = ""
//  }
}

resource "aws_api_gateway_integration_response" "status_ok_integration_resource" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  resource_id = aws_api_gateway_resource.api_resource.id
  http_method = aws_api_gateway_method.api_method.http_method
  status_code = aws_api_gateway_method_response.status_ok_resource.status_code

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
  depends_on = [
    aws_api_gateway_integration.bento_integration,
    aws_api_gateway_integration.proxy_integration
  ]
  rest_api_id = aws_api_gateway_rest_api.api.id
  stage_name  =  var.api_stage_name
}

//#create s3 bucket to host website logs
//resource "aws_s3_bucket" "s3-icdc" {
//  bucket = join(".",["api",var.domain_name])
//  tags = merge(
//  {
//    "Name" = format("%s",var.stack_name)
//  },
//  var.tags,
//  )
//}
//
//#create policy document
//data "aws_iam_policy_document" "api_read_policy" {
//  statement {
//    sid = "apigwRead"
//    actions = [
//      "s3:Get*",
//      "s3:List*"
//    ]
//    resources = [ join("",[aws_s3_bucket.s3-icdc.arn,"/*"])]
//    principals {
//      type = "Service"
//      identifiers = ["apigateway.amazonaws.com"]
//    }
//    effect = "Allow"
//
//  }
//
//}
//
//resource "aws_s3_bucket_policy" "s3_api_read" {
//  bucket = aws_s3_bucket.s3-icdc.id
//  policy = data.aws_iam_policy_document.api_read_policy.json
//}
//
//data "aws_iam_policy_document" "s3_full_access_doc" {
//  statement {
//    sid = "apigwRead0"
//    actions = [
//      "s3:*"
//    ]
//    resources = [ "*"]
//    effect = "Allow"
//
//  }
//}
//
//data "aws_iam_policy_document" "api_gateway_policy" {
//  statement {
//    actions = ["sts:AssumeRole"]
//    effect = "Allow"
//    principals {
//      type        = "Service"
//      identifiers = ["apigateway.amazonaws.com"]
//    }
//  }
//}
//resource "aws_iam_policy" "s3_full_access_policy" {
//  policy = data.aws_iam_policy_document.s3_full_access_doc.json
//  name =  join("-",[var.stack_name,"s3-full-access"])
//  description = "api s3 read access"
//}
//resource "aws_iam_role" "api_iam_role" {
//  name               = "${var.stack_name}-api-gateway-role"
//  path               = "/"
//  assume_role_policy = data.aws_iam_policy_document.api_gateway_policy.json
//}
//
//resource "aws_iam_role_policy_attachment" "api-gateway-attach" {
//  role       = aws_iam_role.api_iam_role.name
//  policy_arn = aws_iam_policy.s3_full_access_policy.arn
//
//}