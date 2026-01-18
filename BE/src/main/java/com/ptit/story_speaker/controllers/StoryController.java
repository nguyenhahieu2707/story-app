package com.ptit.story_speaker.controllers;

import com.ptit.story_speaker.domain.dto.request.*;
import com.ptit.story_speaker.domain.dto.response.ChapterResponse;
import com.ptit.story_speaker.domain.dto.response.StoryCardResponse;
import com.ptit.story_speaker.domain.dto.response.StoryExtractionResponse;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.services.EpubService;
import com.ptit.story_speaker.services.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final EpubService epubService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoryResponse> createStory(@ModelAttribute @Valid StoryCreationRequest request) {
        StoryResponse response = storyService.createStory(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoryResponse> updateStory(@PathVariable String id, @ModelAttribute @Valid StoryUpdateRequest request) {
        StoryResponse response = storyService.updateStory(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/extract-epub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoryExtractionResponse> extractEpub(@RequestParam("file") MultipartFile file) throws IOException {
        StoryExtractionResponse response = epubService.extractStoryFromEpub(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/import-epub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StoryResponse> importEpub(@RequestParam("file") MultipartFile file) throws IOException {
        StoryResponse response = storyService.importEpub(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-extracted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoryResponse> saveExtractedStory(@RequestBody @Valid StorySaveRequest request) {
        StoryResponse response = storyService.saveExtractedStory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StoryCardResponse>> getStoriesForAdmin(
            @RequestParam(required = false) String createdByRole,
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(storyService.getStoriesForAdmin(createdByRole, pageable));
    }

    @PostMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StoryCardResponse>> searchStoriesForAdmin(
            @RequestBody AdminStorySearchRequest request,
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(storyService.searchStoriesForAdmin(request, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<StoryCardResponse>> getStoriesForUser(
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(storyService.getStoriesForUser(pageable));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<StoryCardResponse>> searchStories(
            @RequestBody StorySearchRequest request,
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(storyService.searchStories(request, pageable));
    }

    @GetMapping("/recently-read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<StoryCardResponse>> getRecentlyReadStories(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(storyService.getRecentlyReadStories(pageable));
    }

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<StoryCardResponse>> getFavoriteStories(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(storyService.getFavoriteStories(pageable));
    }

    @PostMapping("/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> favoriteStory(@PathVariable String id) {
        storyService.favoriteStory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-library")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<StoryCardResponse>> getUserLibrary(
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(storyService.getUserLibrary(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> getStoryById(@PathVariable String id) {
        StoryResponse response = storyService.getStoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chapter/{id}")
    public ResponseEntity<ChapterResponse> getChapterById(@PathVariable String id) {
        ChapterResponse response = storyService.getChapterById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteStory(@PathVariable String id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-webview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleWebView(@PathVariable String id) {
        storyService.toggleWebView(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(storyService.getAllCategories());
    }
}
