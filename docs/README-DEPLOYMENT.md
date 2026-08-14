# Deployment Guide

## CI/CD Pipeline Overview

```
GitHub Push
    │
    ├── 1. Test (./gradlew test)
    │
    ├── 2. Build Docker Image (multi-stage build)
    │
    ├── 3. Push to Docker Hub
    │       │
    │       ▼
    │   Docker Hub (username/ticket-backend:latest)
    │
    └── 4. Deploy to EC2 via AWS Systems Manager
            │
            ├── docker compose pull
            ├── docker compose down
            └── docker compose up -d
```

## Prerequisites

### 1. Docker Hub Account
- Create account at https://hub.docker.com
- Create repository: `ticket-backend`

### 2. AWS Setup

#### EC2 Instance
- Ubuntu/Amazon Linux 2 instance
- Docker and Docker Compose installed
- SSM Agent installed and running
- IAM Role attached with `AmazonSSMManagedInstanceCore` policy

#### IAM User for GitHub Actions
Create user with permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ssm:SendCommand",
        "ssm:GetCommandInvocation",
        "ssm:ListCommandInvocations"
      ],
      "Resource": "*"
    }
  ]
}
```

### 3. GitHub Secrets

Add these secrets to your GitHub repository (`Settings` → `Secrets and variables` → `Actions`):

```
DOCKER_USERNAME=your_dockerhub_username
DOCKER_PASSWORD=your_dockerhub_password_or_token

AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=ap-southeast-1
EC2_INSTANCE_ID=i-xxxxxxxxxxxxxxxxx
```

## EC2 Server Setup

### 1. Install Docker and Docker Compose

```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Setup Application Directory

```bash
# Create app directory
mkdir -p /home/ec2-user/ticket-online
cd /home/ec2-user/ticket-online

# Copy docker-compose.yml and .env from your local machine
# Or clone from git and copy files
```

### 3. Create Environment File

```bash
# Copy example and edit with production values
cp .env.example .env
nano .env
```

### 4. Initialize Database (First Time Only)

```bash
# Create init.sql if needed
# Start only MySQL first to run init script
docker compose up -d mysql

# Wait for MySQL to be ready
docker compose logs -f mysql

# Then start all services
docker compose up -d
```

### 5. Verify SSM Agent

```bash
# Check if SSM agent is running
sudo systemctl status amazon-ssm-agent

# If not installed, install it:
sudo yum install -y amazon-ssm-agent
sudo systemctl enable amazon-ssm-agent
sudo systemctl start amazon-ssm-agent
```

## Deployment Workflow

### Automatic Deployment (via GitHub Actions)

1. **Push to main branch**:
```bash
git push origin main
```

2. **Monitor deployment**:
- Go to GitHub Actions tab
- Watch the pipeline execution
- All three jobs will run: Test → Build → Deploy

### Manual Deployment (on EC2)

```bash
# SSH into EC2
ssh ec2-user@your-ec2-ip

# Navigate to app directory
cd /home/ec2-user/ticket-online

# Pull latest image
docker compose pull app

# Restart application
docker compose down app
docker compose up -d app

# Check logs
docker compose logs -f app

# Check health
curl http://localhost:8081/actuator/health
```

## Monitoring

### Check Application Status

```bash
# View running containers
docker compose ps

# View application logs
docker compose logs -f app

# View all services logs
docker compose logs -f

# Check health endpoint
curl http://localhost:8081/actuator/health

# Check Prometheus metrics
curl http://localhost:8081/actuator/prometheus
```

### Database Access

```bash
# Access MySQL
docker compose exec mysql mysql -u ticket_user -p ticket_online

# Backup database
docker compose exec mysql mysqldump -u root -p ticket_online > backup.sql

# Restore database
docker compose exec -T mysql mysql -u root -p ticket_online < backup.sql
```

### Redis Access

```bash
# Access Redis CLI
docker compose exec redis redis-cli -a your_redis_password

# Monitor Redis
docker compose exec redis redis-cli -a your_redis_password MONITOR

# Check keys
docker compose exec redis redis-cli -a your_redis_password KEYS "seat:hold:*"
```

## Rollback

### Rollback to Previous Version

```bash
# On EC2, specify previous image tag
export IMAGE_TAG=main-abc1234
docker compose pull app
docker compose up -d app
```

### Emergency Stop

```bash
# Stop application
docker compose stop app

# Stop all services
docker compose down
```

## Troubleshooting

### Container Not Starting

```bash
# Check logs
docker compose logs app

# Check if ports are available
sudo netstat -tulpn | grep 8081

# Restart with fresh container
docker compose down app
docker compose up -d app
```

### Database Connection Issues

```bash
# Check if MySQL is running
docker compose ps mysql

# Check MySQL logs
docker compose logs mysql

# Test connection from app container
docker compose exec app nc -zv mysql 3306
```

### Redis Connection Issues

```bash
# Check if Redis is running
docker compose ps redis

# Test Redis connection
docker compose exec app nc -zv redis 6379

# Check Redis auth
docker compose exec redis redis-cli -a your_redis_password PING
```

### SSM Deployment Fails

```bash
# On EC2, check SSM agent status
sudo systemctl status amazon-ssm-agent

# Check IAM role attached to EC2
aws sts get-caller-identity

# Manually test SSM command
aws ssm send-command \
  --instance-ids i-xxxxxxxxx \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["echo hello"]'
```

## Performance Tuning

### JVM Settings (in Dockerfile)
```
-XX:MaxRAMPercentage=75.0  # Use 75% of container memory
-XX:+UseG1GC               # Use G1 garbage collector
```

### MySQL Tuning (docker-compose.yml)
```yaml
command: 
  - --max_connections=200
  - --innodb_buffer_pool_size=1G
```

### Redis Tuning
```yaml
command: redis-server 
  --requirepass password 
  --maxmemory 512mb 
  --maxmemory-policy allkeys-lru
```

## Security Best Practices

1. **Never commit .env file** - Use `.env.example` as template
2. **Rotate secrets regularly** - Update JWT_SECRET, database passwords
3. **Use strong passwords** - Minimum 16 characters
4. **Enable firewall** - Only expose necessary ports
5. **Regular updates** - Keep Docker images and system packages updated
6. **Monitor logs** - Set up log aggregation (ELK, CloudWatch)
7. **Enable SSL/TLS** - Use reverse proxy (Nginx) with Let's Encrypt

## Cost Optimization

1. **Use appropriate EC2 instance size** - Start with t3.small, scale as needed
2. **Enable auto-stop during off-hours** - Use AWS Instance Scheduler
3. **Use Docker image caching** - Reduces build time and bandwidth
4. **Clean up unused images** - `docker image prune -a` regularly
