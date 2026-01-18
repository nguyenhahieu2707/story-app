package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.common.exceptions.AppException;
import com.ptit.story_speaker.common.exceptions.ErrorCode;
import com.ptit.story_speaker.services.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "File không được để trống");
        }

        try {
            // 1. Tạo tên file duy nhất (unique)
            // Lấy phần mở rộng (ví dụ: .jpg, .png)
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String objectName = UUID.randomUUID().toString() + "." + extension;

            // 2. Lấy InputStream
            InputStream inputStream = file.getInputStream();

            // 3. Upload file lên MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 4. Trả về URL công khai của file
            return minioUrl + "/" + bucketName + "/" + objectName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload file: " + e.getMessage());
        }
    }

    @Override
    public String uploadStream(InputStream inputStream, long size, String contentType, String objectName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            // Trả về URL công khai
            return minioUrl + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi upload stream lên MinIO: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Trích xuất objectName từ URL
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi xóa file: " + e.getMessage());
        }
    }
}
