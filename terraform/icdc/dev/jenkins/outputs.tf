output "docker_agent_ip" {
  value = "${aws_instance.agent.private_ip}"
}
output "jenkins_ip" {
  value = "${aws_instance.jenkins.private_ip}"
}