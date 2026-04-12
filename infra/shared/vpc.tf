# ──────────────────────────────────────
# VPC
# ──────────────────────────────────────
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = local.name_prefix
  cidr = var.vpc_cidr
  azs  = local.azs

  public_subnets  = var.public_subnet_cidrs
  private_subnets = var.private_subnet_cidrs

  # NAT Instance 사용으로 NAT Gateway 비활성화
  enable_nat_gateway = false

  enable_dns_hostnames = true
  enable_dns_support   = true
}

# ──────────────────────────────────────
# NAT Instance (t4g.nano ~$3/월, NAT Gateway 대신 사용)
# ──────────────────────────────────────
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-arm64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "nat" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t4g.nano"
  subnet_id                   = module.vpc.public_subnets[0]
  associate_public_ip_address = true
  source_dest_check           = false
  vpc_security_group_ids      = [aws_security_group.nat.id]
  iam_instance_profile        = "sclass-nat-ssm-profile"

  user_data = <<-EOF
    #!/bin/bash
    # IP forwarding
    echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
    sysctl -p
    # NAT masquerade + forward rules
    dnf install -y iptables-services
    iptables -t nat -A POSTROUTING -o ens5 -s ${var.vpc_cidr} -j MASQUERADE
    iptables -I FORWARD 1 -s ${var.vpc_cidr} -j ACCEPT
    iptables -I FORWARD 2 -d ${var.vpc_cidr} -m state --state RELATED,ESTABLISHED -j ACCEPT
    service iptables save
    systemctl enable iptables
  EOF

  tags = {
    Name = "${local.name_prefix}-nat"
  }

  lifecycle {
    ignore_changes = [ami]
  }
}

# Private Subnet → NAT Instance 라우팅
resource "aws_route" "private_nat" {
  count                  = length(module.vpc.private_route_table_ids)
  route_table_id         = module.vpc.private_route_table_ids[count.index]
  destination_cidr_block = "0.0.0.0/0"
  network_interface_id   = aws_instance.nat.primary_network_interface_id
}

# ──────────────────────────────────────
# S3 VPC Gateway Endpoint (무료, NAT 없이 S3 접근)
# ──────────────────────────────────────
resource "aws_vpc_endpoint" "s3" {
  vpc_id       = module.vpc.vpc_id
  service_name = "com.amazonaws.${var.aws_region}.s3"

  route_table_ids = module.vpc.private_route_table_ids

  tags = {
    Name = "${local.name_prefix}-s3-endpoint"
  }
}
