package com.resparo.dev.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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

    @Autowired
    @Lazy
    private LineReader lineReader;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // Minimum part size AWS allows is 5MB
    private static final int PART_SIZE = 5 * 1024 * 1024; // 5MB

    public String cloudBackup(InputStream dbDumpStream, String fileName) {
        try {
            byte[] header = dbDumpStream.readNBytes(5);

            if (header.length == 0) {
                return "Backup failed: dump process produced no output";
            }

            // Validate header based on file type
            String validationError = validateDumpHeader(fileName, header);
            if (validationError != null) {
                log.error("Invalid dump header bytes: {}", Arrays.toString(header));
                return validationError;
            }

            log.info("Valid dump archive detected ({}), starting upload...", getDumpType(fileName));

            InputStream fullStream = new SequenceInputStream(
                    new ByteArrayInputStream(header), dbDumpStream);

            return multipartUpload(fullStream, fileName);

        } catch (IOException e) {
            log.error("Backup failed: {}", e.getMessage());
            return "Backup failed: " + e.getMessage();
        }
    }

    // ─── VALIDATION ──────────────────────────────────────────────

    private String validateDumpHeader(String fileName, byte[] header) {
        if (fileName.endsWith(".dump")) {
            // PostgreSQL custom format: magic bytes "PGDMP" → [80, 71, 68, 77, 80]
            String magic = new String(header, StandardCharsets.US_ASCII);
            if (!magic.startsWith("PGDMP")) {
                return "Backup failed: not a valid PostgreSQL dump (expected PGDMP magic bytes)";
            }
        } else if (fileName.endsWith(".sql")) {
            // MySQL/PostgreSQL plain SQL: starts with "--" comment or "/*"
            String start = new String(header, StandardCharsets.US_ASCII);
            if (!start.startsWith("--") && !start.startsWith("/*") && !start.startsWith("SET")) {
                return "Backup failed: not a valid SQL dump (unexpected header)";
            }
        } else {
            // Unknown format — log a warning but don't block
            log.warn("Unknown dump format for file: {}, skipping header validation", fileName);
        }
        return null; // null = valid
    }

    private String getDumpType(String fileName) {
        if (fileName.endsWith(".dump"))
            return "PostgreSQL binary";
        if (fileName.endsWith(".sql"))
            return "SQL plain text";
        return "unknown";
    }
    // ─── MULTIPART UPLOAD (large DBs > 5MB) ──────────────────

    private String multipartUpload(InputStream inputStream, String backupFileName) {
        String uploadId = null;
        try {
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(backupFileName)
                    .build();

            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
            uploadId = createResponse.uploadId();
            log.info("Multipart upload initiated. UploadId: {}", uploadId);

            List<CompletedPart> completedParts = new ArrayList<>();
            byte[] buffer = new byte[PART_SIZE];
            int partNumber = 1;
            int bytesRead;

            while ((bytesRead = readFully(inputStream, buffer)) > 0) {
                byte[] partData = Arrays.copyOf(buffer, bytesRead);

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

            // Edge case: stream was empty
            if (completedParts.isEmpty()) {
                abortMultipartUpload(backupFileName, uploadId);
                return "Backup failed: pg_dump produced no output";
            }

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

    // private String simpleUpload(byte[] backupData, String fileName) {
    // try {
    // PutObjectRequest request = PutObjectRequest.builder()
    // .bucket(bucketName)
    // .key(fileName)
    // .contentLength((long) backupData.length)
    // .build();

    // s3Client.putObject(request, RequestBody.fromBytes(backupData));

    // log.info("Simple upload successful: {}", fileName);
    // return "Backup successful: " + fileName;

    // } catch (S3Exception e) {
    // log.error("Simple upload failed: {}", e.awsErrorDetails().errorMessage());
    // return "Upload failed: " + e.awsErrorDetails().errorMessage();
    // }
    // }

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

    // ---- Download preffered backup file --------------------------
    public InputStream downloadFile(String objectKey) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(objectRequest);

        // Debug: check content length
        byte[] header = s3Object.readNBytes(5);
        String headerString = new String(header);
        log.info("Header string: [{}]", headerString);

        return new SequenceInputStream(
                new ByteArrayInputStream(header),
                s3Object);
    }

    // ─── LIST FILES ───────────────────────────────────────────

    public String listFiles(String databasename, String username) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(username + "_" + databasename)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<String> res = response.contents()
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            if (res.isEmpty()) {
                throw new RuntimeException("No backup files found for given user or database.");
            }

            for (String item : res) {
                System.out.println(item);
            }
            String input = this.lineReader.readLine("select your choice of backup ");
            log.debug(res.get(Integer.parseInt(input)));
            return res.get(Integer.parseInt(input));

        } catch (S3Exception e) {
            log.error("S3 list error: {}", e.awsErrorDetails().errorMessage());
            return e.awsErrorDetails().errorMessage();
            // return List.of("Error listing files: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}