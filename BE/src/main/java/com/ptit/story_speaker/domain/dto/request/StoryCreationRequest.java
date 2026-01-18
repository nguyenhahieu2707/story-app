package com.ptit.story_speaker.domain.dto.request;

import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryCreationRequest {

    @NotBlank(message = "Tiêu đề truyện không được để trống")
    private String title;

    private String author;

    private String description;

    private Integer ageRating;

    private StoryStatus storyStatus;

    private MultipartFile coverImage;

    private List<StoryCategory> categories;

    List<ChapterCreationRequest> chapters;
}
