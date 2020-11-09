# locals {
#   alb_origin_id = "cloudfront_alb_origin"
# }
# resource "aws_cloudfront_distribution" "site_distribution" {
  
#   origin {
    
#     custom_origin_config {
#       http_port              = 80
#       https_port             = 443
#       origin_protocol_policy = "http-only"
#       origin_ssl_protocols   = ["TLSv1.2"]
#     }

#     domain_name = aws_s3_bucket.s3-site.website_endpoint
#     origin_id   = join(".",[var.site,var.domain])
#   }
  
#   origin {
# 	   domain_name = aws_alb.alb.dns_name
# 	   origin_id   = local.alb_origin_id
# 	   custom_origin_config {
# 	    http_port              = 80
# 		  https_port             = 443
# 		  origin_protocol_policy = "https-only"
# 		  origin_ssl_protocols   = ["TLSv1.2"]
# 	   }
#   }

#   enabled             = true
#   default_root_object = var.index_document
#   aliases             = [join(".",[var.site,var.domain])]
#   is_ipv6_enabled     = true

#   default_cache_behavior {
#     viewer_protocol_policy = "redirect-to-https"
#     compress               = true
#     allowed_methods  = ["GET", "HEAD", "OPTIONS"]
#     cached_methods   = ["GET", "HEAD", "OPTIONS"]
    
#     target_origin_id       = join(".",[var.site,var.domain])
#     min_ttl                = 0
#     default_ttl            = 86400
#     max_ttl                = 86400

#     forwarded_values {
#       query_string = false
#       headers      = ["*"]
#       cookies {
#         forward = "none"
#       }
#     }
#   }

#   # Cache behavior
#   ordered_cache_behavior {
#     path_pattern     = "/api/*"
#     allowed_methods  = ["DELETE","GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
#     cached_methods   = ["GET", "HEAD"]
#     target_origin_id = local.alb_origin_id
#     forwarded_values {
#       query_string = true
#       headers      = ["*"]
#       cookies {
#         forward = "all"
#       }
#     }
#     default_ttl            = 0
# 	  min_ttl                = 0
# 	  max_ttl                = 0
#     compress               = true
#     viewer_protocol_policy = "redirect-to-https"
#   }

#   restrictions {
#     geo_restriction {
#       restriction_type = "none"
#     }
#   }

#   viewer_certificate {
#     acm_certificate_arn = data.aws_acm_certificate.certificate.arn
#     ssl_support_method  = "sni-only"
#   }
#   tags = {
#     ProvisionedBy = "Terraform"
#   }
# }