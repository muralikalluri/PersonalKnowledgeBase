package com.knowledge.chat.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        // Uses default credentials provider chain (Environment Variables,
        // ~/.aws/credentials, etc.)
        // This is standard for real AWS usage.
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
