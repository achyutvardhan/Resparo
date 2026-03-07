package com.resparo.dev.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // Minimum part size AWS allows is 5MB
    private static final int PART_SIZE = 5 * 1024 * 1024; // 5MB

    public String cloudBackup(InputStream pgDumpStream, String fileName) {
        try {
            // Read entire stream into byte array first
            byte[] backupData = pgDumpStream.readAllBytes();
            long size = backupData.length;

            log.info("Backup size: {} bytes", size);

            // If less than 5MB — use simple upload
            if (size < PART_SIZE) {
                return simpleUpload(backupData, fileName);
            }

            // If greater than 5MB — use multipart upload
            return multipartUpload(new ByteArrayInputStream(backupData), fileName, size);

        } catch (IOException e) {
            log.error("Backup failed: {}", e.getMessage());
            return "Backup failed: " + e.getMessage();
        }
    }
    // ─── MULTIPART UPLOAD (large DBs > 5MB) ──────────────────

    private String multipartUpload(InputStream inputStream, String backupFileName, long size) {
        String uploadId = null;
        try {
            // Step 1 — Initiate Multipart Upload
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(backupFileName)
                    .build();

            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
            uploadId = createResponse.uploadId();
            log.info("Multipart upload initiated. UploadId: {}", uploadId);

            // Step 2 — Upload parts in chunks
            List<CompletedPart> completedParts = new ArrayList<>();
            byte[] buffer = new byte[PART_SIZE];
            int partNumber = 1;
            int bytesRead;

            while ((bytesRead = readFully(inputStream, buffer)) > 0) {

                byte[] partData = new byte[bytesRead];
                System.arraycopy(buffer, 0, partData, 0, bytesRead);

                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(backupFileName)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength((long) bytesRead)
                        .build();

                UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                        uploadPartRequest,
                        RequestBody.fromBytes(partData));

                completedParts.add(CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build());
                log.info("Uploaded part {} — size: {} bytes", partNumber, bytesRead);
                partNumber++;
            }
            // Step 3 — Complete Multipart Upload
            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(backupFileName)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

            log.info("Backup uploaded successfully to S3: {}", backupFileName);
            return "Backup successful: " + backupFileName;
        } catch (IOException e) {
            // Step 6 — Abort if anything goes wrong
            abortMultipartUpload(backupFileName, uploadId);
            log.error("Backup failed: {}", e.getMessage());
            return "Backup failed: " + e.getMessage();

        } catch (S3Exception e) {
            abortMultipartUpload(backupFileName, uploadId);
            log.error("S3 error: {}", e.awsErrorDetails().errorMessage());
            return "S3 error: " + e.awsErrorDetails().errorMessage();
        }
    }

    // ─── SIMPLE UPLOAD (small DBs < 5MB) ─────────────────────

    private String simpleUpload(byte[] backupData, String fileName) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentLength((long) backupData.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(backupData));

            log.info("Simple upload successful: {}", fileName);
            return "Backup successful: " + fileName;

        } catch (S3Exception e) {
            log.error("Simple upload failed: {}", e.awsErrorDetails().errorMessage());
            return "Upload failed: " + e.awsErrorDetails().errorMessage();
        }
    }

    // ─── ABORT MULTIPART UPLOAD ───────────────────────────────

    private void abortMultipartUpload(String key, String uploadId) {
        if (uploadId == null)
            return;
        try {
            s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .build());
            log.warn("Multipart upload aborted for key: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to abort multipart upload: {}", e.awsErrorDetails().errorMessage());
        }
    }

    // ─── HELPER — Read stream fully up to buffer size ─────────

    private int readFully(InputStream inputStream, byte[] buffer) throws IOException {
        int totalRead = 0;
        int bytesRead;
        while (totalRead < buffer.length &&
                (bytesRead = inputStream.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += bytesRead;
        }
        return totalRead;
    }

    // ─── LIST FILES ───────────────────────────────────────────

    public List<String> listFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents()
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("S3 list error: {}", e.awsErrorDetails().errorMessage());
            return List.of("Error listing files: " + e.awsErrorDetails().errorMessage());
        }
    }

}
