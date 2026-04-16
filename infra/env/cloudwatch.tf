# ──────────────────────────────────────
# CloudWatch Dashboard (prod only)
# ──────────────────────────────────────

locals {
  cw_service_keys = keys(var.services)
}

resource "aws_cloudwatch_dashboard" "main" {
  count = local.is_prod ? 1 : 0

  dashboard_name = "${local.name_prefix}-overview"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "ECS CPU Utilization"
          region = var.aws_region
          period = 300
          metrics = [for key in local.cw_service_keys : [
            "AWS/ECS", "CPUUtilization",
            "ClusterName", local.name_prefix,
            "ServiceName", "${local.name_prefix}-${key}"
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
          title  = "ECS Memory Utilization"
          region = var.aws_region
          period = 300
          metrics = [for key in local.cw_service_keys : [
            "AWS/ECS", "MemoryUtilization",
            "ClusterName", local.name_prefix,
            "ServiceName", "${local.name_prefix}-${key}"
          ]]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "ALB Request Count"
          region = var.aws_region
          period = 300
          metrics = [for key in local.cw_service_keys : [
            "AWS/ApplicationELB", "RequestCount",
            "TargetGroup", aws_lb_target_group.services[key].arn_suffix,
            "LoadBalancer", aws_lb.main[0].arn_suffix
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
          title  = "ALB Target Response Time"
          region = var.aws_region
          period = 300
          metrics = [for key in local.cw_service_keys : [
            "AWS/ApplicationELB", "TargetResponseTime",
            "TargetGroup", aws_lb_target_group.services[key].arn_suffix,
            "LoadBalancer", aws_lb.main[0].arn_suffix
          ]]
        }
      }
    ]
  })
}

# ──────────────────────────────────────
# Alarms — ECS CPU (prod only)
# ──────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  for_each = local.is_prod ? var.services : {}

  alarm_name          = "${local.name_prefix}-${each.key}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "${each.key} ECS CPU > 80%"
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = aws_ecs_cluster.main[0].name
    ServiceName = aws_ecs_service.services[each.key].name
  }
}
