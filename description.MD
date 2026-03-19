# URL Shortener Service

## Overview
A production-grade URL shortener service built with Java 17 + Spring Boot 3, backed by AWS services (DynamoDB, SQS, S3) running locally via LocalStack and Terraform.

## Architecture

```
Client
  │
  ▼
Spring Boot 3 App (:8080)
  ├── POST /api/v1/urls        → Shorten URL
  ├── GET  /{code}             → HTTP 302 Redirect
  ├── GET  /api/v1/urls/{code} → URL info + stats
  └── DELETE /api/v1/urls/{code}
          │
          ├── DynamoDB  (AWS SDK v2 Enhanced Client)
          ├── Redis     (Cache-aside, 60 min TTL)
          └── SQS       (Async click analytics)

Infrastructure (Terraform + LocalStack):
  ├── DynamoDB → url_mappings table (TTL, GSIs)
  ├── SQS     → url-analytics queue + DLQ
  └── S3      → url-shortener-logs bucket
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Primary Store | AWS DynamoDB (via LocalStack) |
| Cache | Redis 7 |
| Messaging | AWS SQS (via LocalStack) |
| Logging Store | AWS S3 (via LocalStack) |
| IaC | Terraform 1.7 |
| Local AWS | LocalStack 3.3 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker / Docker Compose |

## Key Features
- **Auto-generated short codes**: 7-char Base62 from SHA-256 hash of URL + timestamp
- **Custom aliases**: User-defined short codes (e.g. `/gh` → github.com)
- **TTL expiry**: URLs auto-expire via DynamoDB TTL (configurable, default 30 days)
- **Cache-aside pattern**: Redis cache for sub-millisecond redirects
- **Click analytics**: Async SQS events + atomic DynamoDB counter increments
- **Dead-letter queue**: Failed analytics events retried up to 3 times
- **OpenAPI**: Swagger UI at `/swagger-ui.html`
- **Health check**: Actuator at `/actuator/health`

## Project Structure

```
url-shortener/
├── README.md
├── Makefile
├── docker-compose.yml
├── infra/                    # Terraform IaC
│   ├── provider.tf           # AWS provider → LocalStack
│   ├── main.tf               # Module wiring
│   ├── variables.tf
│   ├── outputs.tf
│   ├── terraform.tfvars
│   └── modules/
│       ├── dynamodb/         # url_mappings table
│       ├── sqs/              # url-analytics + DLQ
│       └── s3/               # url-shortener-logs bucket
└── service/                  # Spring Boot 3 App
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/com/urlshortener/
        │   ├── UrlShortenerApplication.java
        │   ├── config/         # AwsConfig, RedisConfig, OpenApiConfig
        │   ├── controller/     # UrlController (REST endpoints)
        │   ├── service/        # UrlShortenerService, AnalyticsService
        │   ├── repository/     # UrlRepository (DynamoDB)
        │   ├── listener/       # ClickEventListener (SQS)
        │   ├── model/          # UrlMapping, ClickEvent
        │   ├── dto/            # ShortenRequest/Response, ErrorResponse
        │   ├── exception/      # Custom exceptions + GlobalExceptionHandler
        │   └── util/           # Base62Encoder
        └── test/               # Unit + MVC slice tests
```

## Quick Start

```bash
# 1. Start LocalStack + Redis
make up

# 2. Provision AWS resources
make tf-apply

# 3. Run the app
make app-run

# 4. Test
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.google.com"}'

# 5. Redirect
curl -L http://localhost:8080/<code>
```

## Data Model

### DynamoDB: `url_mappings`

| Attribute | Type | Description |
|-----------|------|-------------|
| `code` | String (PK) | Short URL code |
| `originalUrl` | String | Original long URL |
| `shortUrl` | String | Full short URL |
| `createdAt` | String | ISO-8601 creation timestamp |
| `expiresAt` | Number | Unix epoch TTL (DynamoDB auto-delete) |
| `clickCount` | Number | Total redirect count |
| `customAlias` | String | User-defined alias (optional) |

GSIs: `originalUrl-index`, `createdAt-index`

## API Reference

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/urls` | Shorten a URL |
| GET | `/{code}` | Redirect to original URL (302) |
| GET | `/api/v1/urls/{code}` | Get URL info + stats |
| DELETE | `/api/v1/urls/{code}` | Delete a short URL |