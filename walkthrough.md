# URL Shortener Walkthrough

## Overview
We have successfully developed and verified the URL shortener service built with **Java 17, Spring Boot 3**, and fully integrated with AWS services via **LocalStack**. All components are functional, and the persistent problems stemming from incompatibilities between Lombok and the user's local Java 24 environment have been completely resolved by implementing all boilerplate features natively within the entities, DTOs, and services.

## What Was Completed

### The Application Layer
- **Lombok Removal**: Removed all `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Slf4j` annotations across the codebase.
- **Manual Boilerplate**: Manually implemented getters, setters, constructors, builders, and standard `LoggerFactory` initializations mapping precisely to the behavior previously provided by Lombok. 
- **Tests Passing**: Mockito's MockMaker was aligned properly to not conflict with Java 24, resulting in a cohesive test suite (`mvn clean test`) passing uniformly.

### The Infrastructure Layer
- **Docker Compose Setup**: Provided a comprehensive mechanism to spin up LocalStack and Redis seamlessly within custom bridge networks.
- **Terraform Integration via Docker**: Implemented an automated Terraform Docker container configuration within [docker-compose.yml](file:///Users/hungnguyen/Desktop/url-shoterner/docker-compose.yml) to provision AWS resources upon initial LocalStack startup without relying on a local `terraform` binary.
- **Resources Provisioned**: Successfully created a `url_mappings` DynamoDB table and a `url-analytics` SQS queue. 
- **Application Start**: [Makefile](file:///Users/hungnguyen/Desktop/url-shoterner/Makefile) commands (`make up` and `make app-run`) were refined to correctly hook up the Spring Boot app configuration endpoints with LocalStack components dynamically.

## What Was Tested

Verification confirmed successful communication from the Spring Boot app into the Docker-defined instances:
- **Shortening Endpoints**: Validated by successfully posting long URLs and receiving distinct [Base62](file:///Users/hungnguyen/Desktop/url-shoterner/service/src/main/java/com/urlshortener/util/Base62Encoder.java#10-69) generated short codes mapped efficiently against DynamoDB.
- **Redirection**: Demonstrated functional operation by checking `curl -v http://localhost:8080/{code}` which yielded clean `HTTP 302` redirects natively tracking telemetry data into SQS.
- **Cache Invalidation**: Verified that a `DELETE /api/v1/urls/{code}` simultaneously purged the mapping directly from DynamoDB as well as caching within Redis, ensuring sequential GET requests respond with `HTTP 404`.

## Commands Executed for Full Demo

1. Turn on LocalStack Infrastructure *(and implicitly provisions Terraform defined AWS resources)*:
   ```bash
   make up
   ```
2. Spawn the Spring Boot App *(using `local` Spring Profile)*:
   ```bash
   make app-run
   ```
3. Test Health Endpoints:
   ```bash
   curl -s http://localhost:8080/actuator/health | jq .
   ```
4. Submitting a URL to Shorten:
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/urls -H "Content-Type: application/json" -d '{"originalUrl":"https://www.google.com"}' | jq .
   ```
5. Test Output Response:
   ```json
   {
      "code": "ENZ9tKn",
      "shortUrl": "http://localhost:8080/ENZ9tKn",
      "originalUrl": "https://www.google.com",
      "createdAt": "2026-03-19T03:45:53.390412Z",
      "expiresAt": "2026-04-18T03:45:53.390412Z",
      "clickCount": 0
   }
   ```
