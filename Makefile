.PHONY: help up down restart logs tf-init tf-plan tf-apply tf-destroy app-build app-run app-test clean

LOCALSTACK_ENDPOINT := http://localhost:4566
AWS_OPTS := AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test aws --endpoint-url=$(LOCALSTACK_ENDPOINT) --region us-east-1

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ─── Docker Compose ─────────────────────────────────────────────────────────
up: ## Start all services (LocalStack, Redis, app)
	docker compose up -d localstack redis
	@echo "Waiting for LocalStack to be healthy..."
	@until docker compose exec localstack curl -sf http://localhost:4566/_localstack/health > /dev/null 2>&1; do sleep 2; done
	@echo "LocalStack is ready!"

down: ## Stop all services
	docker compose down -v

restart: down up ## Restart all services

logs: ## Tail logs from all services
	docker compose logs -f

app-up: up tf-apply ## Start infra + provision + run app locally
	@echo "Infrastructure ready. Starting app..."
	cd service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# ─── Terraform ───────────────────────────────────────────────────────────────
tf-init: ## Initialize Terraform
	cd infra && terraform init

tf-plan: ## Terraform plan (against LocalStack)
	cd infra && terraform plan -var-file=terraform.tfvars

tf-apply: tf-init ## Apply Terraform config to LocalStack
	cd infra && terraform apply -auto-approve -var-file=terraform.tfvars

tf-destroy: ## Destroy all Terraform-managed resources
	cd infra && terraform destroy -auto-approve -var-file=terraform.tfvars

tf-show: ## Show current Terraform state
	cd infra && terraform show

# ─── AWS CLI helpers (via LocalStack) ────────────────────────────────────────
aws-list-tables: ## List DynamoDB tables
	$(AWS_OPTS) dynamodb list-tables

aws-list-queues: ## List SQS queues
	$(AWS_OPTS) sqs list-queues

aws-list-buckets: ## List S3 buckets
	$(AWS_OPTS) s3api list-buckets

aws-scan-urls: ## Scan all records in url_mappings table
	$(AWS_OPTS) dynamodb scan --table-name url_mappings

# ─── Spring Boot App ─────────────────────────────────────────────────────────
app-build: ## Build Spring Boot app (skip tests)
	cd service && mvn clean package -DskipTests

app-test: ## Run unit tests
	cd service && mvn test

app-run: ## Run app locally with 'local' profile
	cd service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# ─── Full Stack (Docker) ──────────────────────────────────────────────────────
stack-up: ## Build and start full stack via Docker Compose
	docker compose up --build -d

stack-down: ## Tear down full Docker stack
	docker compose down -v --remove-orphans

# ─── Quick Test ──────────────────────────────────────────────────────────────
test-shorten: ## Quick test: shorten a URL
	@curl -s -X POST http://localhost:8080/api/v1/urls \
		-H "Content-Type: application/json" \
		-d '{"originalUrl":"https://www.google.com"}' | jq .

test-health: ## Check app health
	@curl -s http://localhost:8080/actuator/health | jq .

# ─── Cleanup ─────────────────────────────────────────────────────────────────
clean: ## Remove build artifacts
	cd service && mvn clean
	rm -rf infra/.terraform infra/.terraform.lock.hcl infra/terraform.tfstate*

load-test: ## Run API load tests using k6 in Docker
	docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 -v "$(PWD)/performance:/scripts" grafana/k6 run /scripts/load-test.js
