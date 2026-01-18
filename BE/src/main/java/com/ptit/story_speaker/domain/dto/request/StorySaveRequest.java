package com.ptit.story_speaker.domain.dto.request;

import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorySaveRequest {

    @NotBlank(message = "Tiêu đề truyện không được để trống")
    private String title;

    private String author;

    private String description;

    private Integer ageRating;

    private String coverImageUrl; // Now a URL, not a file

    private StoryStatus status;

    private List<StoryCategory> categories;

    private List<ChapterSaveRequest> chapters;
}
