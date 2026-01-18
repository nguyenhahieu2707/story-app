package com.ptit.story_speaker.domain.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoiceTrainRequest {
    private String name;
    private MultipartFile file;
    private String f0Method;     // Map từ f0_method
    private String epochsNumber; // Map từ epochs_number
    private String userId;       // Map từ user_id
    private String trainAt;
    private String locate;
}