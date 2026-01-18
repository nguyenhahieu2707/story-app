package com.ptit.story_speaker.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StoryGenerationResponse {
    private String story;
}
