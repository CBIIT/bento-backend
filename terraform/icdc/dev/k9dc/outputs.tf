# output "tomcat_ip" {
#   value = "${element(aws_instance.k9dc.*.private_ip,0)}"
# }
# output "tomcat_ip_1" {
#   value = "${element(aws_instance.k9dc.*.private_ip,1)}"
# }

output "tomcat01_ip" {
  value = "${element(data.aws_instances.k9dc.private_ips,0)}"
}
output "tomcat02_ip" {
  value = "${element(data.aws_instances.k9dc.private_ips,1)}"
}

output "neo4j_ip" {
  value = "${data.aws_instance.neo4j.private_ip}"
}
