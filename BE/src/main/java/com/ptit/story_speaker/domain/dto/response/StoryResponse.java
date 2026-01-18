package com.ptit.story_speaker.domain.dto.response;

import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {
    private String id;
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
    private String ageRating;
    private Boolean isFavorite;
    private StoryStatus status;
    private String uploaderName;
    private LocalDateTime createDate;
    private LocalDateTime lastUpdateDate;
    private List<String> categoryNames;
    private List<ChapterResponse> chapters;
}
