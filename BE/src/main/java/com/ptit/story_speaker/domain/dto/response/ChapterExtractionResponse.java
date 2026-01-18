package com.ptit.story_speaker.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterExtractionResponse {
    private String title;
    private Integer chapterNumber;
    private String content;
}
