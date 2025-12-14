package com.knowledge.chat.ingestion.config;

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

        // Load existing if available to append
        File file = new File(VECTOR_STORE_PATH);
        if (file.exists()) {
            simpleVectorStore.load(file);
        }

        return simpleVectorStore;
    }
}
