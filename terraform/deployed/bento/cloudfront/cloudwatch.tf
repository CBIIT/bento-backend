resource "aws_cloudwatch_metric_alarm" "foobar" {
  for_each = var.alarms
  alarm_name                = "${var.stack_name}-${terraform.workspace}-${each.key}-cloudfront-alarm"
  comparison_operator       = "GreaterThanOrEqualToThreshold"
  evaluation_periods        = "60"
  metric_name               = each.value["name"]
  namespace                 = "AWS/CloudFront"
  period                    = "60"
  statistic                 = "Average"
  threshold                 = each.value["threshold"]
  alarm_description         = "CloudFront alarm for ${each.value["name"]}"
  insufficient_data_actions = []
  dimensions = {
    DistributionId = aws_cloudfront_distribution.bento_distribution.id
    Region         = "Global"
  }
}