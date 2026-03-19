# URL Shortener Service

A production-grade **URL Shortener** built with **Java 17 + Spring Boot 3**, backed by **AWS services** (DynamoDB, SQS, S3) running locally via **LocalStack**, with **Redis** as a caching layer.

---

## Architecture

```
Client
  │
  ▼
Spring Boot 3 App (:8080)
  ├── POST /api/v1/urls          → Shorten URL
  ├── GET  /{code}               → HTTP 302 Redirect
  ├── GET  /api/v1/urls/{code}   → URL info + click stats
  └── DELETE /api/v1/urls/{code} → Delete
          │
          ├── DynamoDB  (primary store via AWS SDK v2 Enhanced Client)
          ├── Redis     (cache-aside, 60 min TTL)
          └── SQS       (async click event analytics)

Infrastructure (LocalStack @ localhost:4566 & Production Ready):
  ├── VPC          → Custom VPC with Public Subnets & IGW
  ├── ALB          → Application Load Balancer with HTTP Listener
  ├── ECS (Fargate)→ Service with Auto Scaling (2-10 instances)
  ├── DynamoDB     → url_mappings table
  ├── SQS          → url-analytics queue + DLQ
  └── S3           → url-shortener-logs bucket
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Docker + Compose | 24+ |
| Terraform | 1.6+ |
| AWS CLI | 2+ (for debugging) |

---

## Quick Start

### 1. Start Infrastructure (LocalStack + Redis)

```bash
make up
```

### 2. Provision AWS Resources (Terraform)

```bash
make tf-apply
```

Verify resources were created:
```bash
make aws-list-tables     # → url_mappings
make aws-list-queues     # → url-analytics, url-analytics-dlq
make aws-list-buckets    # → url-shortener-logs
```

### 3. Run the Application

```bash
make app-run
```

Or all in one:
```bash
make app-up
```

### 4. Test the API

**Shorten a URL:**
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com"}'
```

Response:
```json
{
  "code": "abc1234",
  "shortUrl": "http://localhost:8080/abc1234",
  "originalUrl": "https://www.google.com",
  "createdAt": "2024-01-01T00:00:00Z",
  "expiresAt": "2024-01-31T00:00:00Z",
  "clickCount": 0
}
```

**Follow Redirect:**
```bash
curl -L http://localhost:8080/abc1234
```

**Custom alias + TTL:**
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com", "customCode": "gh", "ttlDays": 7}'
```

**Get stats:**
```bash
curl http://localhost:8080/api/v1/urls/abc1234
```

**Delete:**
```bash
curl -X DELETE http://localhost:8080/api/v1/urls/abc1234
```

---

## Swagger UI

Open: **http://localhost:8080/swagger-ui.html**

---

## Full Docker Stack

```bash
make stack-up    # Build + start everything via Docker Compose
make stack-down  # Tear down
```

---

## Run Tests

```bash
make app-test
```

---

## Load Testing

The project includes a load testing script using [k6](https://k6.io/) to simulate high traffic, verify cache latency bindings, and ensure High Availability metrics are met. The script automatically executes within a Docker container, so no native installation of k6 is required.

Run the load test:
```bash
make load-test
```

---

## Project Structure

```
url-shortener/
├── Makefile                         # Developer shortcuts
├── docker-compose.yml               # LocalStack + Redis + App
├── infra/                           # Terraform IaC
│   ├── provider.tf                  # AWS provider → LocalStack
│   ├── main.tf                      # Module wiring
│   ├── variables.tf / outputs.tf
│   ├── terraform.tfvars
│   ├── modules/
│   │   ├── vpc/                     # Networking (VPC, Subnets, SG)
│   │   ├── alb/                     # Load Balancing (ALB, TG, Listener)
│   │   ├── ecs/                     # Compute (Fargate, Tasks, IAM, Scaling)
│   │   ├── dynamodb/                # url_mappings table
│   │   ├── sqs/                     # url-analytics + DLQ
│   │   └── s3/                      # url-shortener-logs bucket
└── service/                         # Spring Boot 3 App
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/urlshortener/
        ├── config/                  # AWS, Redis, OpenAPI config
        ├── controller/              # REST endpoints
        ├── service/                 # Business logic + analytics
        ├── repository/              # DynamoDB repository
        ├── listener/                # SQS click event listener
        ├── model/                   # UrlMapping, ClickEvent
        ├── dto/                     # Request/Response DTOs
        └── util/                    # Base62Encoder
```

---

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `APP_BASE_URL` | `http://localhost:8080` | Base URL for short links |
| `AWS_ENDPOINT_URL` | `http://localhost:4566` | LocalStack endpoint |
| `AWS_REGION` | `us-east-1` | AWS region |
| `SPRING_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `app.cache.url-ttl-minutes` | `60` | Redis cache TTL |
| `app.dynamodb.default-ttl-days` | `30` | URL expiry in days |

---

## Technologies

- **Java 17** + **Spring Boot 3.2**
- **AWS SDK v2** (DynamoDB Enhanced Client)
- **Spring Cloud AWS 3.1** (SQS listener)
- **Redis** (cache-aside via Spring Data Redis)
- **Terraform 1.7** + **LocalStack 3.3**
- **SpringDoc OpenAPI** (Swagger UI)
- **Lombok**, **Jackson**, **Spring Validation**
