# High-Level Design (HLD)

## 1. System Overview
The URL Shortener service is designed to take a long URL and generate a fast, concise, customized short link that redirects clients efficiently. 
The system leverages modern cloud-native principles configured natively via **Terraform** provisioning AWS backing services like **DynamoDB**, **ElastiCache (Redis)**, and **Amazon SQS**, running behind **Java 17 / Spring Boot 3**.

## 2. Infrastructure Architecture (Terraform Managed)
The core infrastructure of the service relies on standard AWS components provisioned by Terraform.
- **API Container (Spring Boot Java 17)**: Compute layer scaling easily behind Application Load Balancers.
- **Amazon DynamoDB (`url_mappings`)**: Primary persistent storage tier maintaining mapping consistency across all container replica sets. Exposes robust Read/Write capacity configurations for high scale.
- **Amazon ElastiCache / Redis**: Distributed high-throughput in-memory caching layer that mitigates consistent READ load hitting the DynamoDB persistent tier.
- **Amazon SQS (`url-analytics`)**: Asynchronous, decoupled job queue to publish off-band telemetry statistics (such as link clicks and user-agent information) without deteriorating the critical path of the actual high-speed client redirect request. Paired with `url-analytics-dlq` (Dead-Letter Queue).
- **Amazon S3 (`url-shortener-logs`)**: Data lake bucket meant to hold application/traffic logs across cold, deep-storage formats managed by Terraform s3 lifecycle policies.

### 2.1 Component Interaction Diagram

```mermaid
flowchart TD
    Client((Client App/Browser))
    LB[Load Balancer / Ingress]
    App[Spring Boot Application Area]
    Cache[(Redis Cache Layer)]
    DB[(DynamoDB url_mappings)]
    SQS[SQS Queue url-analytics]
    DLQ[SQS DLQ url-analytics-dlq]

    %% Main Request Flow
    Client -->|HTTP POST /api/v1/urls| LB
    Client -->|HTTP GET /{code} Redirect| LB
    
    LB --> App
    
    %% Compute -> Storage interactions 
    App <-->|1. Check/Set Cache| Cache
    App <-->|2. Resolve/Write Entity| DB
    
    %% Async interactions
    App -.->|3. Publish async event| SQS
    SQS -.->|4. Route msg failure| DLQ
```

## 3. Scale and Availability Requirements
- **High Read-to-Write Ratio**: The service assumes that URLs are created sporadically but redirected heavily. Therefore, the caching layer acts as the absolute frontline, aggressively hitting Redis with 1 hour caching TTL to serve sub-millisecond resolutions.
- **Write Consistency**: When generating a Base62 UUID, the app employs optimistic handling for uniqueness against DynamoDB.
- **Telemetry Deflection**: Capturing user-clicks on millions of redirects slows down responses trivially. Therefore, the architecture segregates analytics events exclusively via Amazon SQS event-driven architecture, decoupled natively by Spring Cloud AWS plugins.

## 4. Security
- **Data Encapsulation**: Endpoints don't disclose sequential IDs in URLs due to random generation logic or explicit semantic custom codes constraint tracking.
- **Network Boundaries**: SQS, DynamoDB, and Redis remain enclosed within virtual private networks, un-exposed to the public internet edge configurations.
