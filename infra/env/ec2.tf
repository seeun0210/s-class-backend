# ──────────────────────────────────────
# EC2 Resources (dev only — docker-compose)
# ──────────────────────────────────────

resource "aws_security_group" "dev_ec2" {
  count       = local.is_prod ? 0 : 1
  name_prefix = "${local.name_prefix}-ec2-"
  description = "Dev EC2 docker-compose"
  vpc_id      = local.shared.vpc_id

  # HTTP (ALB 없이 직접 접근)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH (EC2 Instance Connect)
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # App ports (supporters-api, backoffice-api)
  ingress {
    from_port   = 8080
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.name_prefix}-ec2"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# EC2 → RDS (shared)
resource "aws_security_group_rule" "dev_ec2_to_rds" {
  count = local.is_prod ? 0 : 1

  type                     = "egress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.dev_ec2[0].id
  source_security_group_id = aws_security_group.rds.id
  description              = "Dev EC2 to RDS MySQL"
}

resource "aws_security_group_rule" "rds_from_dev_ec2" {
  count = local.is_prod ? 0 : 1

  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = aws_security_group.dev_ec2[0].id
  description              = "RDS MySQL from Dev EC2"
}

# ──────────────────────────────────────
# EC2 Instance
# ──────────────────────────────────────
data "aws_ami" "amazon_linux_dev" {
  count       = local.is_prod ? 0 : 1
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

resource "aws_instance" "dev" {
  count = local.is_prod ? 0 : 1

  ami                         = data.aws_ami.amazon_linux_dev[0].id
  instance_type               = var.dev_ec2_instance_type
  subnet_id                   = local.shared.public_subnets[0]
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.dev_ec2[0].id]
  iam_instance_profile        = aws_iam_instance_profile.dev_ec2[0].name
  key_name                    = var.dev_ec2_key_name != "" ? var.dev_ec2_key_name : null

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = <<-USERDATA
    #!/bin/bash
    set -e
    dnf update -y

    # SSM Agent + EC2 Instance Connect
    dnf install -y amazon-ssm-agent ec2-instance-connect
    systemctl enable amazon-ssm-agent
    systemctl start amazon-ssm-agent

    # Docker
    dnf install -y docker
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ec2-user

    # Docker Compose plugin
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # ECR login helper
    dnf install -y amazon-ecr-credential-helper
    mkdir -p /home/ec2-user/.docker
    echo '{"credsStore":"ecr-login"}' > /home/ec2-user/.docker/config.json
    chown -R ec2-user:ec2-user /home/ec2-user/.docker

    # Nginx
    dnf install -y nginx
    systemctl enable nginx

    # Certbot (Let's Encrypt)
    dnf install -y certbot python3-certbot-nginx

    # Nginx initial config (HTTP only, certbot will add SSL)
    cat > /etc/nginx/conf.d/sclass.conf << 'NGINX'
    server {
        listen 80;
        server_name supporters-api.${var.dev_ec2_domain};

        location / {
            proxy_pass http://localhost:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }

    server {
        listen 80;
        server_name backoffice-api.${var.dev_ec2_domain};

        location / {
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
    NGINX

    # Remove default server block
    rm -f /etc/nginx/conf.d/default.conf
    systemctl start nginx

    # Issue SSL certs (requires DNS to be pointed to this EC2 first)
    if [ -n "${var.dev_certbot_email}" ] && [ -n "${var.dev_ec2_domain}" ]; then
      certbot --nginx --non-interactive --agree-tos \
        --email "${var.dev_certbot_email}" \
        -d "supporters-api.${var.dev_ec2_domain}" \
        -d "backoffice-api.${var.dev_ec2_domain}" \
        || echo "Certbot failed - DNS may not be ready yet. Run manually: certbot --nginx -d supporters-api.${var.dev_ec2_domain} -d backoffice-api.${var.dev_ec2_domain}"

      # Auto-renewal cron
      echo "0 3 * * * root certbot renew --quiet" > /etc/cron.d/certbot-renew
    fi
  USERDATA

  tags = {
    Name = "${local.name_prefix}-app"
  }

  lifecycle {
    ignore_changes = [ami, user_data]
  }
}
