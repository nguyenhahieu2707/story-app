package com.ptit.story_speaker.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String errorCode,
        String error,
        String message,
        String path,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String errorCode, String error, String message, String path) {
        this(status, errorCode, error, message, path, LocalDateTime.now());
    }
}