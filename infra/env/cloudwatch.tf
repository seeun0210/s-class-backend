# ──────────────────────────────────────
# CloudWatch Dashboard + Alarms
# ──────────────────────────────────────

locals {
  cw_service_keys = keys(var.services)
}

# ──────────────────────────────────────
# Dashboard (전체 서비스 통합 Overview)
# ──────────────────────────────────────
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${local.name_prefix}-overview"

  dashboard_body = jsonencode({
    widgets = [
      # Row 1: JVM Heap + CPU
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "JVM Heap Memory Used"
          region = var.aws_region
          period = 300
          metrics = [for i, key in local.cw_service_keys : [
            {
              expression = "SUM(SEARCH('{SClass/${title(key)},area,id} MetricName=\"jvm.memory.used\" area=\"heap\"', 'Average', 300))"
              id         = "heap${i}"
              label      = key
            }
          ]]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "CPU Usage"
          region = var.aws_region
          period = 300
          yAxis  = { left = { min = 0, max = 1 } }
          metrics = [for i, key in local.cw_service_keys : [
            {
              expression = "SEARCH('{SClass/${title(key)}} MetricName=\"process.cpu.usage\"', 'Average', 300)"
              id         = "cpu${i}"
              label      = key
            }
          ]]
        }
      },
      # Row 2: HikariCP + HTTP Requests
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "HikariCP Active Connections"
          region = var.aws_region
          period = 300
          metrics = [for i, key in local.cw_service_keys : [
            {
              expression = "SEARCH('{SClass/${title(key)},pool} MetricName=\"hikaricp.connections.active\"', 'Average', 300)"
              id         = "hikari${i}"
              label      = key
            }
          ]]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "HTTP Request Count"
          region = var.aws_region
          period = 300
          metrics = [for i, key in local.cw_service_keys : [
            {
              expression = "SUM(SEARCH('{SClass/${title(key)},exception,method,outcome,status,uri} MetricName=\"http.server.requests\"', 'SampleCount', 300))"
              id         = "http${i}"
              label      = key
            }
          ]]
        }
      }
    ]
  })
}

# ──────────────────────────────────────
# Alarms — CPU 사용률 (서비스별)
# ──────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  for_each = var.services

  alarm_name          = "${local.name_prefix}-${each.key}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "process.cpu.usage"
  namespace           = "SClass/${title(each.key)}"
  period              = 300
  statistic           = "Average"
  threshold           = 0.8
  alarm_description   = "${each.key} CPU usage > 80%"
  treat_missing_data  = "notBreaching"
}

# ──────────────────────────────────────
# Alarms — HikariCP Active Connections (서비스별)
# ──────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "hikari_connections_high" {
  for_each = var.services

  alarm_name          = "${local.name_prefix}-${each.key}-hikari-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "hikaricp.connections.active"
  namespace           = "SClass/${title(each.key)}"
  period              = 300
  statistic           = "Average"
  threshold           = 4
  alarm_description   = "${each.key} HikariCP active connections > 4 (pool max: 5)"
  treat_missing_data  = "notBreaching"

  dimensions = {
    pool = "HikariPool-1"
  }
}
