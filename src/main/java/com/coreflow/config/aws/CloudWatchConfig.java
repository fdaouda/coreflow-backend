package com.coreflow.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import java.net.URI;

@Configuration
public class CloudWatchConfig {

    @Value("${aws.region:eu-west-3}")
    private String region;

    @Value("${aws.s3.endpoint:http://localhost:4566}")
    private String endpoint;

    @Value("${aws.s3.access-key:test}")
    private String accessKey;

    @Value("${aws.s3.secret-key:test}")
    private String secretKey;

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient() {
        return CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }
}
