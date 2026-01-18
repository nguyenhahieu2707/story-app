package com.ptit.story_speaker.services;

import com.ptit.story_speaker.domain.dto.response.ChapterExtractionResponse;
import com.ptit.story_speaker.domain.dto.response.StoryExtractionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface EpubService {
    StoryExtractionResponse extractStoryFromEpub(MultipartFile epubFile) throws IOException;
}
