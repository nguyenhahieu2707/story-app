package com.ptit.story_speaker.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterSaveRequest {

    private String title;

    private Integer chapterNumber;

    private String content; // HTML content
}
