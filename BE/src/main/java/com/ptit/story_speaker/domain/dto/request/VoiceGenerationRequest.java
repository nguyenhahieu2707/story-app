package com.ptit.story_speaker.domain.dto.request;

import lombok.Data;

@Data
public class VoiceGenerationRequest {
    private String text;
    private String voiceId;
    private String language;
}