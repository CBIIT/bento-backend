resource "aws_cloudwatch_metric_alarm" "cloudfront_alarm" {
  for_each = var.alarms
  alarm_name                = "${var.stack_name}-${terraform.workspace}-${each.key}-cloudfront-alarm"
  comparison_operator       = "GreaterThanOrEqualToThreshold"
  evaluation_periods        = "5"
  metric_name               = each.value["name"]
  namespace                 = "AWS/CloudFront"
  period                    = "60"
  statistic                 = "Sum"
  threshold                 = each.value["threshold"]
  alarm_description         = "CloudFront alarm for ${each.value["name"]}"
  insufficient_data_actions = []
  dimensions = {
    DistributionId = aws_cloudfront_distribution.bento_distribution.id
    Region         = "Global"
  }
  alarm_actions       = [aws_sns_topic.cloudfront_alarm_topic.arn]
  ok_actions          = [aws_sns_topic.cloudfront_alarm_topic.arn]
}


resource "aws_sns_topic" "cloudfront_alarm_topic" {
  name = "${var.stack_name}-${terraform.workspace}-cloudfront-4xx-5xx-errors"
  delivery_policy = <<EOF
{
  "http": {
    "defaultHealthyRetryPolicy": {
      "minDelayTarget": 20,
      "maxDelayTarget": 20,
      "numRetries": 3,
      "numMaxDelayRetries": 0,
      "numNoDelayRetries": 0,
      "numMinDelayRetries": 0,
      "backoffFunction": "linear"
    },
    "disableSubscriptionOverrides": false,
    "defaultThrottlePolicy": {
      "maxReceivesPerSecond": 1
    }
  }
}
EOF
}

resource "aws_sns_topic_subscription" "subscribe_slack_endpoint" {
  endpoint               = aws_lambda_function.slack_lambda.arn
  protocol               = "lambda"
  endpoint_auto_confirms = true
  topic_arn              = aws_sns_topic.cloudfront_alarm_topic.arn
}

resource "aws_cloudwatch_log_group" "log_group_waf" {
  name              = "/aws/lambda/${aws_lambda_function.slack_waf.function_name}"
  retention_in_days = 30
  tags = var.tags
}

resource "aws_cloudwatch_log_group" "log_group_slack" {
  name              = "/aws/lambda/${aws_lambda_function.slack_lambda.function_name}"
  retention_in_days = 30
  tags = var.tags
}