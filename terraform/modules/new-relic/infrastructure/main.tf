
resource "newrelic_alert_policy" "alert_policy" {
  name = var.alert_policy_name #"ppdc_disk_utilization"
  incident_preference = var.incident_preference #"PER_CONDITION"  #var.incident_preference
}

# Creates an email alert channel.
resource "newrelic_alert_channel" "email_channel" {
  name = var.email_channel #"ppdc_email_channel"
  type = "email"

  config {
    recipients              = var.recipients  #"foo@example.com"  # need to check for the alias
    include_json_attachment = "1"
  }
}

# Creates a Slack alert channel.
resource "newrelic_alert_channel" "slack_channel" {
  name = var.slack_channel #"ppdc-monitoring"
  type = "slack"

  config {
    channel = var.slack_channel_name #"ppdc_monitoring"
    url     = var.slack_url #"https://join.slack.com/share/zt-tcihyl3b-N7orX2KeHwZZU_5fW1kN1g"
  }
}



# Applies the created channels above to the alert policy
# referenced at the top of the config.
resource "newrelic_alert_policy_channel" "newrelic_alert_notification_channel" {
  policy_id  = newrelic_alert_policy.alert_policy.id
  channel_ids = [
    newrelic_alert_channel.email_channel.id,
    newrelic_alert_channel.slack_channel.id
  ]
}

resource "newrelic_infra_alert_condition" "high_disk_usage" {
  policy_id = newrelic_alert_policy.alert_policy.id

  name        = "High disk usage"
  description = "Warning if disk usage goes above 80% and critical alert if goes above 90%"
  type        = "infra_metric"
  event       = "StorageSample"
  select      = "diskUsedPercent"
  comparison  = "above"
  where       = var.host_condition #"(hostname LIKE '%ppdc*frontend%')"

  critical {
    duration      = 25
    value         = 90
    time_function = "all"
  }

  warning {
    duration      = 10
    value         = 80
    time_function = "all"
  }
}

resource "newrelic_infra_alert_condition" "cpu_percent_utilization" {
  policy_id = newrelic_alert_policy.alert_policy.id

  name        = "High disk usage"
  description = "Warning if disk usage goes above 80% and critical alert if goes above 90%"
  type        = "infra_metric"
  event       = "StorageSample"
  select      = "cpuPercent"
  comparison  = "below"
  where       = var.host_condition #"(hostname LIKE '%ppdc*frontend%')"

  critical {
    duration      = 2
    value         = 10
    time_function = "all"
  }

  warning {
    duration      = 2
    value         = 20
    time_function = "all"
  }
}

resource "newrelic_infra_alert_condition" "host_not_reporting" {
  policy_id = newrelic_alert_policy.alert_policy.id

  name        = "Host not reporting"
  description = "Critical alert when the host is not reporting"
  type        = "infra_host_not_reporting"
  where       = var.host_condition    #"(hostname LIKE '%frontend%')"

  critical {
    duration = 5
  }
}

