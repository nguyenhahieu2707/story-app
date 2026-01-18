package com.ptit.story_speaker.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterCreationRequest {

    private String title;

    private Integer chapterNumber;

    private String content;

    private MultipartFile contentFile;

    private List<MultipartFile> illustrationImages;
}