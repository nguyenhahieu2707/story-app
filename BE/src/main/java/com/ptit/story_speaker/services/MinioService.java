package com.ptit.story_speaker.services;

import java.io.InputStream;

public interface MinioService {
    String uploadFile(InputStream inputStream, String fileName, String contentType);
    void removeFile(String fileUrl);
}
