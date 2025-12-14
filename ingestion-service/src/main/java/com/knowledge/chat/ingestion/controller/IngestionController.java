package com.knowledge.chat.ingestion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    private final VectorStore vectorStore;
    private final S3Client s3Client;
    private final String BUCKET_NAME = "knowledge-base-files";

    public IngestionController(VectorStore vectorStore, S3Client s3Client) {
        this.vectorStore = vectorStore;
        this.s3Client = s3Client;
    }

    @PostMapping
    public ResponseEntity<String> ingestDocument(@RequestParam("key") String key) {
        log.info("Starting ingestion for key: {}", key);

        try {
            // 1. Fetch file from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            Resource resource = new ByteArrayResource(objectBytes.asByteArray());

            // 2. Parse PDF/Document using Tika
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            List<Document> documents = tikaDocumentReader.get();

            // 3. Split into chunks
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(documents);

            // 4. Store Embeddings in Vector DB
            // Add metadata about the source file
            for (Document doc : splitDocuments) {
                doc.getMetadata().put("source_file", key);
            }
            vectorStore.add(splitDocuments);

            // Critical for SimpleVectorStore: Persist to File
            if (vectorStore instanceof org.springframework.ai.vectorstore.SimpleVectorStore) {
                ((org.springframework.ai.vectorstore.SimpleVectorStore) vectorStore).save(
                        new java.io.File(com.knowledge.chat.ingestion.config.VectorStoreConfig.VECTOR_STORE_PATH));
            }

            log.info("Ingestion completed for key: {}", key);
            return ResponseEntity.ok("Ingestion successful. Processed " + splitDocuments.size() + " chunks.");

        } catch (Exception e) {
            log.error("Error ingesting document", e);
            return ResponseEntity.internalServerError().body("Error ingesting document: " + e.getMessage());
        }
    }
}
