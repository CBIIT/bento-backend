/*module "infrastructure" {
  source = "../../../modules/new-relic/infrastructure"
}
*/
/*
# Creates an email alert channel.
resource "newrelic_alert_channel" "email_channel" {
  name = var.email_channel
  type = "email"

  config {
    recipients              = var.recipients  #"foo@example.com"  # need to check for the alias
    include_json_attachment = "1"
  }
}

# Creates a Slack alert channel.
resource "newrelic_alert_channel" "slack_channel" {
  name = var.slack_channel
  type = "slack"

  config {
    channel = var.slack_channel_name
    url     = var.slack_url
  }
}
*/
resource "newrelic_alert_policy" "alert_policy_disk_utilization_db" {
  name = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-DB-disk-utilization"      #var.alert_policy_disk_utilization_name #"ppdc_disk_utilization"
  incident_preference = var.incident_preference
  channel_ids = [
    newrelic_alert_channel.email_channel.id,
    newrelic_alert_channel.slack_channel.id
  ]
}

resource "newrelic_alert_policy" "alert_policy_cpu_usage_db" {
  name = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-DB-cpu-usage"  #var.alert_policy_cpu_usage_name #"ppdc_disk_utilization"
  incident_preference = var.incident_preference
  channel_ids = [
    newrelic_alert_channel.email_channel.id,
    newrelic_alert_channel.slack_channel.id
  ]
}

resource "newrelic_alert_policy" "alert_policy_host_reporting_db" {
  name = "${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-DB-host-not-reporting"  #"ppdc_disk_utilization"
  incident_preference = var.incident_preference
  channel_ids = [
    newrelic_alert_channel.email_channel.id,
    newrelic_alert_channel.slack_channel.id
  ]
}

resource "newrelic_infra_alert_condition" "high_disk_usage_db" {
  policy_id = newrelic_alert_policy.alert_policy_disk_utilization_db.id

  name        = "High disk usage"
  description = "Warning if disk usage goes above 80% and critical alert if goes above 90%"
  type        = "infra_metric"
  event       = "StorageSample"
  select      = "diskUsedPercent"
  comparison  = "above"
  where       = "(hostname LIKE '%${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-database%')"

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

resource "newrelic_infra_alert_condition" "cpu_percent_utilization_db" {
  policy_id = newrelic_alert_policy.alert_policy_cpu_usage_db.id

  name        = "High disk usage"
  description = "Warning if disk usage goes above 80% and critical alert if goes above 90%"
  type        = "infra_metric"
  event       = "StorageSample"
  select      = "cpuPercent"
  comparison  = "below"
  where       = "(hostname LIKE '%${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-database%')"

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

resource "newrelic_infra_alert_condition" "host_not_reporting_db" {
  policy_id = newrelic_alert_policy.alert_policy_host_reporting_db.id

  name        = "Host not reporting"
  description = "Critical alert when the host is not reporting"
  type        = "infra_host_not_reporting"
  where       = "(hostname LIKE '%${var.stack_name}-${var.frontend_app_name}-frontend-${var.env}-database%')"

  critical {
    duration = 5
  }
}


