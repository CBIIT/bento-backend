output "frontend_asg_name" {
  value = aws_autoscaling_group.asg_frontend.name
  description = "Autoscaling group name"
}
output "processor_asg_name" {
  value = aws_autoscaling_group.asg_processor.name
  description = "Autoscaling group name"
}
output "frontend_launch_configuration_name" {
  value = aws_launch_configuration.asg_launch_config_frontend.name
}
output "processor_launch_configuration_name" {
  value = aws_launch_configuration.asg_launch_config_processor.name
}