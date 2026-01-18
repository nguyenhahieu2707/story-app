package com.ptit.story_speaker.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {
    String uploadFile(MultipartFile file);

    String uploadStream(InputStream inputStream, long size, String contentType, String objectName);

    void deleteFile(String fileUrl);
}
