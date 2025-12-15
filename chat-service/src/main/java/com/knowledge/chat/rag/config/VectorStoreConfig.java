package com.knowledge.chat.rag.config;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    public static final String VECTOR_STORE_PATH = "../vector-store.json";

    @Bean
    public VectorStore vectorStore(EmbeddingClient embeddingClient) {
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingClient);

        File file = new File(VECTOR_STORE_PATH);
        if (file.exists()) {
            simpleVectorStore.load(file);
        }

        return simpleVectorStore;
    }

    // Explicitly configure Titan Embedding Client to force text-mode
    @Bean
    public org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingClient bedrockTitanEmbeddingClient(
            @org.springframework.beans.factory.annotation.Value("${spring.ai.bedrock.aws.region}") String region,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {

        software.amazon.awssdk.auth.credentials.AwsCredentialsProvider credentialsProvider = software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
                .create();

        org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi titanApi = new org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi(
                "amazon.titan-embed-text-v1",
                credentialsProvider,
                region,
                objectMapper);

        return new org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingClient(titanApi);
    }
}
