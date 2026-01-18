package com.ptit.story_speaker.domain.dto.request;

import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import lombok.Data;

import java.util.List;

@Data
public class StorySearchRequest {
    private String keyword;
    private List<StoryCategory> categories;
    private String author;
    private Integer ageRating;
}
