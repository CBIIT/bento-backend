output "es_endpoint" {
  value = aws_elasticsearch_domain.es.endpoint
}

output "es_kibana_endpoint" {
  value = aws_elasticsearch_domain.es.kibana_endpoint
}