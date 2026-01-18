package com.ptit.story_speaker.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoiceResponse {
    private String id;
    private String name;
    private String modelId;
    private LocalDateTime createDate;
}
