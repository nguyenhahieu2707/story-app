package com.ptit.story_speaker.domain.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class StoryGenerationRequest {
    private String title;
    private String freeText;
    private Integer durationSeconds;
    private String readingLevel;
    private String genre;
    private List<CharacterInput> characters;
    private String tone;
    private List<String> keyMessages;
    private Boolean includeSoundCues;
    private String language;
    private String additionalInstructions;
}