package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.services.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucketName)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build());
            // Construct the URL manually
            return minioUrl + "/" + defaultBucketName + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage());
        }
    }

    @Override
    public void removeFile(String fileUrl) {
        try {
            // Sử dụng URI để parse path, tránh phụ thuộc vào domain (localhost vs 127.0.0.1)
            URI uri = new URI(fileUrl);
            String path = uri.getPath(); // Trả về /bucket/object/name...

            if (path.startsWith("/")) {
                path = path.substring(1); // bucket/object/name...
            }

            int firstSlashIndex = path.indexOf('/');
            if (firstSlashIndex > 0) {
                String extractedBucketName = path.substring(0, firstSlashIndex);
                String rawObjectName = path.substring(firstSlashIndex + 1);
                
                // Decode URL để lấy tên object gốc (ví dụ xử lý %20, dấu cách, v.v.)
                String objectName = URLDecoder.decode(rawObjectName, StandardCharsets.UTF_8);

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(extractedBucketName)
                                .object(objectName)
                                .build()
                );
                log.info("Removed file from MinIO bucket {}: {}", extractedBucketName, objectName);
            } else {
                log.warn("Invalid MinIO URL path format, cannot extract bucket and object name: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("Error removing file from MinIO: {}", fileUrl, e);
        }
    }
}
