package com.ptit.story_speaker.services;

import com.ptit.story_speaker.domain.dto.request.*;
import com.ptit.story_speaker.domain.dto.response.ChapterResponse;
import com.ptit.story_speaker.domain.dto.response.StoryCardResponse;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StoryService {
    StoryResponse createStory(StoryCreationRequest request);
    StoryResponse updateStory(String id, StoryUpdateRequest request);
    StoryResponse saveExtractedStory(StorySaveRequest request);
    StoryResponse importEpub(MultipartFile file) throws IOException;
    Page<StoryCardResponse> getStoriesForAdmin(String createdByRole, Pageable pageable);
    Page<StoryCardResponse> getStoriesForUser(Pageable pageable);
    Page<StoryCardResponse> searchStories(StorySearchRequest request, Pageable pageable);
    Page<StoryCardResponse> searchStoriesForAdmin(AdminStorySearchRequest request, Pageable pageable);
    Page<StoryCardResponse> getRecentlyReadStories(Pageable pageable);
    Page<StoryCardResponse> getFavoriteStories(Pageable pageable);
    Page<StoryCardResponse> getUserLibrary(Pageable pageable);
    StoryResponse getStoryById(String id);
    ChapterResponse getChapterById(String id);
    void favoriteStory(String storyId);
    void deleteStory(String storyId);
    void toggleWebView(String storyId);
    long countStories();
    List<String> getAllCategories();
}
