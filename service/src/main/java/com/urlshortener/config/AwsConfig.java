package com.urlshortener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.endpoint-url:}")
    private String endpointUrl;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.credentials.access-key:test}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:test}")
    private String secretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        // Override endpoint for LocalStack
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
