package com.ptit.story_speaker.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryCardResponse {
    private String id;
    private String title;
    private String author;
    private String coverImageUrl;
    private Boolean isFavorite;
    private Boolean webView;
    private String status;
}
