package com.knowledge.chat.ingestion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

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

    @org.springframework.beans.factory.annotation.Value("${application.bucket}")
    private String bucketName;

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
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            // Fix: Store bytes to temp file as PdfReader requires random access file
            java.io.File tempFile = java.io.File.createTempFile("ingest-", ".pdf");
            java.nio.file.Files.write(tempFile.toPath(), objectBytes.asByteArray());

            List<Document> documents;
            try {
                Resource resource = new org.springframework.core.io.FileSystemResource(tempFile);
                org.springframework.ai.reader.pdf.PagePdfDocumentReader pdfReader = new org.springframework.ai.reader.pdf.PagePdfDocumentReader(
                        resource);
                documents = pdfReader.get();
            } finally {
                tempFile.delete();
            }

            // 3. Split into chunks
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(documents);

            // 4. Store Embeddings in Vector DB
            // Add metadata about the source file
            for (Document doc : splitDocuments) {
                doc.getMetadata().put("source_file", key);
            }

            if (!splitDocuments.isEmpty()) {
                log.info("About to embed {} documents", splitDocuments.size());
                log.info("First document content sample (first 200 chars): {}",
                        splitDocuments.get(0).getContent().substring(0,
                                Math.min(splitDocuments.get(0).getContent().length(), 200)));
                log.info("First document metadata: {}", splitDocuments.get(0).getMetadata());
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
            if (e instanceof software.amazon.awssdk.services.bedrockruntime.model.ValidationException) {
                return ResponseEntity.badRequest().body("Bedrock Validation Error: " + e.getMessage());
            }
            return ResponseEntity.internalServerError().body("Error ingesting document: " + e.getMessage());
        }
    }

    @jakarta.annotation.PostConstruct
    public void logConfig() {
        log.info("Ingestion Controller Initialized");
        log.info("Bucket Name: {}", bucketName);

        if (vectorStore != null) {
            log.info("Vector Store Type: {}", vectorStore.getClass().getName());
        }
    }
}
