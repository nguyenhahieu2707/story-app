package com.ptit.story_speaker.domain.dto.response;

import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryExtractionResponse {
    private String title;
    private String author;
    private String description;
    private Integer ageRating;
    private String coverImageUrl;
    private List<StoryCategory> categories;
    private List<ChapterExtractionResponse> chapters;
}
