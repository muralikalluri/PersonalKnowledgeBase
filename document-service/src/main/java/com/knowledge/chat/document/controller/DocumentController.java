package com.knowledge.chat.document.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final S3Client s3Client;
    private final String BUCKET_NAME = "knowledge-base-files";

    public DocumentController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // In a real event-driven architecture, we would emit an event here (e.g.,
        // Kafka/RabbitMQ)
        // For simplicity in this logical separation, we might call the ingestion
        // service directly or assume a shared DB trigger.
        // For now, we'll return the key so the frontend can trigger ingestion or just
        // confirming storage.

        return ResponseEntity.ok(key);
    }

    @GetMapping
    public ResponseEntity<List<String>> listDocuments() {
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
        List<String> fileNames = listRes.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileNames);
    }
}
