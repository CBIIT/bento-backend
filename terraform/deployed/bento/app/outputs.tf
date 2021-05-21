
output "configuration_endpoint_address" {
  value = aws_elasticache_replication_group.replication_group.configuration_endpoint_address
}
//output "configuration_endpoint_address" {
//  value = aws_elasticache_cluster.redis_cluster.configuration_endpoint
//}