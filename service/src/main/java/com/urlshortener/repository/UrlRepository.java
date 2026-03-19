package com.urlshortener.repository;

import com.urlshortener.model.UrlMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Repository
public class UrlRepository {

    private static final Logger log = LoggerFactory.getLogger(UrlRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Value("${app.dynamodb.table-name:url_mappings}")
    private String tableName;

    private DynamoDbTable<UrlMapping> table;

    public UrlRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    }

    @PostConstruct
    public void init() {
        table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(UrlMapping.class));
        log.info("Initialized DynamoDB table: {}", tableName);
    }

    public void save(UrlMapping urlMapping) {
        log.debug("Saving URL mapping: code={}", urlMapping.getCode());
        table.putItem(urlMapping);
    }

    public Optional<UrlMapping> findByCode(String code) {
        log.debug("Looking up URL mapping: code={}", code);
        Key key = Key.builder().partitionValue(code).build();
        UrlMapping result = table.getItem(key);
        return Optional.ofNullable(result);
    }

    public void delete(String code) {
        log.debug("Deleting URL mapping: code={}", code);
        Key key = Key.builder().partitionValue(code).build();
        table.deleteItem(key);
    }

    /**
     * Atomically increments the clickCount for a given code.
     * Uses DynamoDB's ADD operation for atomic counter update.
     */
    public void incrementClickCount(String code) {
        log.debug("Incrementing click count for code={}", code);
        dynamoDbClient.updateItem(
                UpdateItemRequest.builder()
                        .tableName(tableName)
                        .key(Map.of("code", AttributeValue.fromS(code)))
                        .updateExpression("ADD clickCount :inc")
                        .expressionAttributeValues(Map.of(
                                ":inc", AttributeValue.fromN("1")
                        ))
                        .returnValues(ReturnValue.NONE)
                        .build()
        );
    }
}
