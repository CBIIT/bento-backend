tags = {
  Project = "ICDC"
  CreatedWith = "Terraform"
  POC = "vincent.donkor@nih.gov"
  Account = "NonProd"
}
stack_name = "icdc"
functions = {
  start_function = {
    function_name = "start-ec2-instances"
    function_package_nane = "start.zip"
    cloudwatch_event_rule_description = "start ec2 instances every 7am"
    cloudwatch_event_rule_name = "start-ec2-instances"
    cloudwatch_event_rule = "cron(0 7 ? * MON-FRI *)"
  },
  stop_function = {
    function_name = "stop-ec2-instances"
    function_package_nane = "stop.zip"
    cloudwatch_event_rule_description = "stop ec2 instances every 8pm"
    cloudwatch_event_rule_name = "stop-ec2-instances"
    cloudwatch_event_rule = "cron(0 20 ? * MON-FRI *)"
  }
}
