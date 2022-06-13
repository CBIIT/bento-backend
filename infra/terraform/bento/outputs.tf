output "db_private_ip" {
  value = module.neo4j.private_ip
}
output "opensearch_endpoint" {
  value = module.opensearch.opensearch_endpoint
}
