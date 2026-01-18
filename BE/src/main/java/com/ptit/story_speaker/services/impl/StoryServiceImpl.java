package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.common.exceptions.AppException;
import com.ptit.story_speaker.common.exceptions.ErrorCode;
import com.ptit.story_speaker.domain.dto.request.*;
import com.ptit.story_speaker.domain.dto.response.ChapterResponse;
import com.ptit.story_speaker.domain.dto.response.StoryCardResponse;
import com.ptit.story_speaker.domain.dto.response.StoryExtractionResponse;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.domain.entity.*;
import com.ptit.story_speaker.domain.mapper.ChapterMapper;
import com.ptit.story_speaker.domain.mapper.StoryMapper;
import com.ptit.story_speaker.domain.model.enums.Role;
import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import com.ptit.story_speaker.repository.ChapterRepository;
import com.ptit.story_speaker.repository.StoryRepository;
import com.ptit.story_speaker.repository.TemporaryFileRepository;
import com.ptit.story_speaker.repository.UserRepository;
import com.ptit.story_speaker.repository.UserStoryLibraryRepository;
import com.ptit.story_speaker.services.EpubService;
import com.ptit.story_speaker.services.MinioService;
import com.ptit.story_speaker.services.StoryService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final UserStoryLibraryRepository userStoryLibraryRepository;
    private final MinioService minioService;
    private final StoryMapper storyMapper;
    private final ChapterMapper chapterMapper;
    private final EpubService epubService;
    private final TemporaryFileRepository temporaryFileRepository;

    @Override
    @Transactional
    public StoryResponse createStory(StoryCreationRequest request) {
        UserEntity uploader = getCurrentUser();

        String coverImageUrl = null;
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                MultipartFile coverImage = request.getCoverImage();
                String fileName = "cover-" + UUID.randomUUID().toString() + "-" + coverImage.getOriginalFilename();
                coverImageUrl = minioService.uploadFile(coverImage.getInputStream(), fileName, coverImage.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload cover image", e);
            }
        }

        StoryEntity newStory = new StoryEntity();
        newStory.setTitle(request.getTitle());
        newStory.setAuthor(request.getAuthor());
        newStory.setDescription(request.getDescription());
        newStory.setAgeRating(request.getAgeRating());
        newStory.setCoverImageUrl(coverImageUrl);
        newStory.setStatus(StoryStatus.DRAFT);
        newStory.setUploader(uploader);
        newStory.setCategories(request.getCategories());

        StoryEntity savedStory = storyRepository.save(newStory);

        if (request.getChapters() != null) {
            List<ChapterEntity> chaptersToSave = new ArrayList<>();
            for (ChapterCreationRequest chapReq : request.getChapters()) {
                ChapterEntity chapter = new ChapterEntity();
                chapter.setStory(savedStory);
                chapter.setChapterNumber(chapReq.getChapterNumber());
                chapter.setTitle(chapReq.getTitle());
                chapter.setContent(chapReq.getContent());

                if (chapReq.getIllustrationImages() != null && !chapReq.getIllustrationImages().isEmpty()) {
                    StringBuilder contentWithImages = new StringBuilder(chapter.getContent() != null ? chapter.getContent() : "");
                    for (MultipartFile imgFile : chapReq.getIllustrationImages()) {
                        try {
                            String fileName = "illustration-" + UUID.randomUUID().toString() + "-" + imgFile.getOriginalFilename();
                            String imgUrl = minioService.uploadFile(imgFile.getInputStream(), fileName, imgFile.getContentType());
                            contentWithImages.append(String.format("<br/><img src='%s' style='max-width:100%%'/><br/>", imgUrl));
                        } catch (IOException e) {
                        }
                    }
                    chapter.setContent(contentWithImages.toString());
                }
                chaptersToSave.add(chapter);
            }
            chapterRepository.saveAll(chaptersToSave);
        }

        return storyMapper.toStoryResponse(savedStory);
    }

    @Override
    @Transactional
    public StoryResponse updateStory(String id, StoryUpdateRequest request) {
        StoryEntity story = storyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy truyện"));

        if (request.getTitle() != null) story.setTitle(request.getTitle());
        if (request.getAuthor() != null) story.setAuthor(request.getAuthor());
        if (request.getDescription() != null) story.setDescription(request.getDescription());
        if (request.getAgeRating() != null) story.setAgeRating(request.getAgeRating());
        if (request.getStoryStatus() != null) story.setStatus(request.getStoryStatus());
        if (request.getCategories() != null) story.setCategories(request.getCategories());

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                MultipartFile coverImage = request.getCoverImage();
                String fileName = "cover-" + UUID.randomUUID().toString() + "-" + coverImage.getOriginalFilename();
                String coverImageUrl = minioService.uploadFile(coverImage.getInputStream(), fileName, coverImage.getContentType());
                story.setCoverImageUrl(coverImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload cover image", e);
            }
        }

        StoryEntity savedStory = storyRepository.save(story);

        // Handle Chapters Synchronization (Add, Update, Delete)
        if (request.getChapters() != null) {
            // 1. Identify chapters to keep/update and new chapters
            Set<String> requestChapterIds = request.getChapters().stream()
                    .map(ChapterUpdateRequest::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 2. Delete chapters that are not in the request
            List<ChapterEntity> existingChapters = story.getChapters();
            
            List<ChapterEntity> chaptersToDelete = existingChapters.stream()
                    .filter(c -> !requestChapterIds.contains(c.getId()))
                    .collect(Collectors.toList());
            
            chapterRepository.deleteAll(chaptersToDelete);
            existingChapters.removeAll(chaptersToDelete);

            // 3. Update existing and Create new chapters
            List<ChapterEntity> chaptersToSave = new ArrayList<>();
            
            for (ChapterUpdateRequest chapReq : request.getChapters()) {
                ChapterEntity chapter;
                if (chapReq.getId() != null) {
                    // Update existing
                    chapter = existingChapters.stream()
                            .filter(c -> c.getId().equals(chapReq.getId()))
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Chapter not found in story context"));
                } else {
                    // Create new
                    chapter = new ChapterEntity();
                    chapter.setStory(savedStory);
                }

                if (chapReq.getTitle() != null) chapter.setTitle(chapReq.getTitle());
                if (chapReq.getChapterNumber() != null) chapter.setChapterNumber(chapReq.getChapterNumber());
                if (chapReq.getContent() != null) chapter.setContent(chapReq.getContent());

                // Handle illustration images (Append to existing content)
                if (chapReq.getIllustrationImages() != null && !chapReq.getIllustrationImages().isEmpty()) {
                    StringBuilder contentWithImages = new StringBuilder(chapter.getContent() != null ? chapter.getContent() : "");
                    for (MultipartFile imgFile : chapReq.getIllustrationImages()) {
                        try {
                            String fileName = "illustration-" + UUID.randomUUID().toString() + "-" + imgFile.getOriginalFilename();
                            String imgUrl = minioService.uploadFile(imgFile.getInputStream(), fileName, imgFile.getContentType());
                            contentWithImages.append(String.format("<br/><img src='%s' style='max-width:100%%'/><br/>", imgUrl));
                        } catch (IOException e) {
                            // Log or handle exception
                        }
                    }
                    chapter.setContent(contentWithImages.toString());
                }
                chaptersToSave.add(chapter);
            }
            chapterRepository.saveAll(chaptersToSave);
        } else {
            if (request.getChapters() != null && request.getChapters().isEmpty()) {
                 chapterRepository.deleteAll(story.getChapters());
            }
        }

        return storyMapper.toStoryResponse(storyRepository.findById(id).orElse(savedStory));
    }

    @Override
    @Transactional
    public StoryResponse saveExtractedStory(StorySaveRequest request) {
        UserEntity uploader = getCurrentUser();

        StoryEntity newStory = new StoryEntity();
        newStory.setTitle(request.getTitle());
        newStory.setAuthor(request.getAuthor());
        newStory.setDescription(request.getDescription());
        newStory.setAgeRating(request.getAgeRating());
        newStory.setCoverImageUrl(request.getCoverImageUrl());
        newStory.setStatus(StoryStatus.DRAFT);
        newStory.setUploader(uploader);
        newStory.setCategories(request.getCategories());
        newStory.setStatus(request.getStatus());

        // Confirm cover image if it exists
        if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().isEmpty()) {
            temporaryFileRepository.deleteByFileUrl(request.getCoverImageUrl());
        }

        StoryEntity savedStory = storyRepository.save(newStory);

        if (request.getChapters() != null) {
            List<ChapterEntity> chaptersToSave = new ArrayList<>();
            for (ChapterSaveRequest chapReq : request.getChapters()) {
                ChapterEntity chapter = new ChapterEntity();
                chapter.setStory(savedStory);
                chapter.setChapterNumber(chapReq.getChapterNumber());
                chapter.setTitle(chapReq.getTitle());

                String processedContent = processChapterContent(chapReq.getContent());
                chapter.setContent(processedContent);

                // Xác nhận các ảnh trong content là chính thức (xóa khỏi bảng tạm)
                confirmUsedImages(processedContent);

                chaptersToSave.add(chapter);
            }
            chapterRepository.saveAll(chaptersToSave);
        }

        return storyMapper.toStoryResponse(savedStory);
    }

    @Override
    @Transactional
    public StoryResponse importEpub(MultipartFile file) throws IOException {
        StoryExtractionResponse extracted = epubService.extractStoryFromEpub(file);

        StorySaveRequest saveRequest = new StorySaveRequest();
        saveRequest.setTitle(extracted.getTitle() != null ? extracted.getTitle() : "Untitled Story");
        saveRequest.setAuthor(extracted.getAuthor());
        saveRequest.setDescription(extracted.getDescription());
        saveRequest.setAgeRating(extracted.getAgeRating());
        saveRequest.setCoverImageUrl(extracted.getCoverImageUrl());
        saveRequest.setCategories(extracted.getCategories());
        saveRequest.setStatus(StoryStatus.DRAFT);

        if (extracted.getChapters() != null) {
            List<ChapterSaveRequest> chapterRequests = extracted.getChapters().stream().map(chap -> {
                ChapterSaveRequest cReq = new ChapterSaveRequest();
                cReq.setTitle(chap.getTitle());
                cReq.setChapterNumber(chap.getChapterNumber());
                cReq.setContent(chap.getContent());
                return cReq;
            }).toList();
            saveRequest.setChapters(chapterRequests);
        }

        return saveExtractedStory(saveRequest);
    }

    private String processChapterContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        Document doc = Jsoup.parse(htmlContent);
        Elements images = doc.select("img");

        for (Element img : images) {
            String src = img.attr("src");
            if (src.startsWith("data:image")) {
                // Decode Base64 image
                String[] parts = src.split(",");
                String mimeType = parts[0].substring(parts[0].indexOf(":") + 1, parts[0].indexOf(";"));
                String base64Image = parts[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Upload to MinIO
                String fileName = "image-" + UUID.randomUUID().toString();
                String imageUrl = minioService.uploadFile(new ByteArrayInputStream(imageBytes), fileName, mimeType);

                // Replace src with MinIO URL
                img.attr("src", imageUrl);
            }
        }
        return doc.body().html();
    }

    private void confirmUsedImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) return;

        Document doc = Jsoup.parse(htmlContent);
        Elements images = doc.select("img");

        for (Element img : images) {
            String src = img.attr("src");
            if (src != null && !src.startsWith("data:image")) {
                temporaryFileRepository.deleteByFileUrl(src);
            }
        }
    }


    @Override
    public Page<StoryCardResponse> getStoriesForAdmin(String createdByRole, Pageable pageable) {
        Specification<StoryEntity> spec = (root, query, cb) -> {
            if (StringUtils.hasText(createdByRole)) {
                try {
                    Role role = Role.valueOf(createdByRole.toUpperCase());
                    Join<StoryEntity, UserEntity> uploaderJoin = root.join("uploader");
                    return cb.equal(uploaderJoin.get("role"), role);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        };

        Page<StoryEntity> storyPage = storyRepository.findAll(spec, pageable);
        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> getStoriesForUser(Pageable pageable) {
        Specification<StoryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Condition 1: webView = true
            predicates.add(cb.isTrue(root.get("webView")));
            
            // Condition 2: uploader.role = ADMIN
            Join<StoryEntity, UserEntity> uploaderJoin = root.join("uploader");
            predicates.add(cb.equal(uploaderJoin.get("role"), Role.ADMIN));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StoryEntity> storyPage = storyRepository.findAll(spec, pageable);
        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> searchStories(StorySearchRequest request, Pageable pageable) {
        Specification<StoryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Condition 1: webView = true (Added)
            predicates.add(cb.isTrue(root.get("webView")));

            // Join with UserEntity to filter by uploader's role
            Join<StoryEntity, UserEntity> uploaderJoin = root.join("uploader");
            predicates.add(cb.equal(uploaderJoin.get("role"), Role.ADMIN));

            if (StringUtils.hasText(request.getKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + request.getKeyword().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(request.getAuthor())) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + request.getAuthor().toLowerCase() + "%"));
            }

            if (request.getAgeRating() != null) {
                predicates.add(cb.equal(root.get("ageRating"), request.getAgeRating()));
            }

            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                predicates.add(root.join("categories").in(request.getCategories()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StoryEntity> storyPage = storyRepository.findAll(spec, pageable);
        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> searchStoriesForAdmin(AdminStorySearchRequest request, Pageable pageable) {
        Specification<StoryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by Keyword (Title OR Author)
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), keyword);
                Predicate authorLike = cb.like(cb.lower(root.get("author")), keyword);
                predicates.add(cb.or(titleLike, authorLike));
            }

            // 2. Filter by CreatedByRole
            if (StringUtils.hasText(request.getCreatedByRole())) {
                try {
                    Role role = Role.valueOf(request.getCreatedByRole().toUpperCase());
                    Join<StoryEntity, UserEntity> uploaderJoin = root.join("uploader");
                    predicates.add(cb.equal(uploaderJoin.get("role"), role));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid role
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StoryEntity> storyPage = storyRepository.findAll(spec, pageable);
        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> getRecentlyReadStories(Pageable pageable) {
        UserEntity currentUser = getCurrentUser();
        // Updated to use the new repository method
        Page<UserStoryLibraryEntity> libraryEntries = userStoryLibraryRepository
                .findByUserIdAndLastSeenNotNullOrderByLastSeenDesc(currentUser.getId(), pageable);

        Page<StoryEntity> storyPage = libraryEntries.map(UserStoryLibraryEntity::getStory);

        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> getFavoriteStories(Pageable pageable) {
        UserEntity currentUser = getCurrentUser();
        Page<UserStoryLibraryEntity> libraryEntries = userStoryLibraryRepository
                .findByUserIdAndIsFavoriteTrueOrderByLastUpdateDateDesc(currentUser.getId(), pageable);

        Page<StoryEntity> storyPage = libraryEntries.map(UserStoryLibraryEntity::getStory);

        return mapToStoryCardResponse(storyPage);
    }

    @Override
    public Page<StoryCardResponse> getUserLibrary(Pageable pageable) {
        UserEntity currentUser = getCurrentUser();
        Page<StoryEntity> storyPage = storyRepository.findByUploaderId(currentUser.getId(), pageable);

        return mapToStoryCardResponse(storyPage);
    }

    private Page<StoryCardResponse> mapToStoryCardResponse(Page<StoryEntity> storyPage) {
        UserEntity currentUser = getCurrentUserOpt();
        Set<String> favoriteStoryIds = Collections.emptySet();

        if (currentUser != null && !storyPage.isEmpty()) {
            List<String> storyIdsInPage = storyPage.getContent().stream()
                    .map(StoryEntity::getId)
                    .toList();
            favoriteStoryIds = userStoryLibraryRepository.findFavoriteStoryIdsByUserIdAndStoryIds(currentUser.getId(), storyIdsInPage);
        }

        Set<String> finalFavoriteStoryIds = favoriteStoryIds;
        return storyPage.map(story -> StoryCardResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .author(story.getAuthor())
                .coverImageUrl(story.getCoverImageUrl())
                .status(story.getStatus() != null ? story.getStatus().name() : "")
                .webView(story.getWebView())
                .isFavorite(finalFavoriteStoryIds.contains(story.getId()))
                .build());
    }

    @Override
    @Transactional
    public StoryResponse getStoryById(String id) {
        StoryEntity story = storyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy truyện"));

        StoryResponse response = storyMapper.toStoryResponse(story);

        if (story.getCategories() != null) {
            List<String> categories = story.getCategories().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            response.setCategoryNames(categories);
        }

        UserEntity currentUser = getCurrentUserOpt();
        if (currentUser != null) {
            // Check favorite status
            boolean isFavorite = userStoryLibraryRepository
                    .existsByUserIdAndStoryIdAndIsFavoriteTrue(currentUser.getId(), id);
            response.setIsFavorite(isFavorite);

            // Update lastSeen
            UserStoryLibraryEntity libraryEntry = userStoryLibraryRepository
                    .findByUserIdAndStoryId(currentUser.getId(), id)
                    .orElseGet(() -> {
                        UserStoryLibraryEntity newEntry = new UserStoryLibraryEntity();
                        newEntry.setUser(currentUser);
                        newEntry.setStory(story);
                        return newEntry;
                    });
            
            libraryEntry.setLastSeen(LocalDateTime.now());
            userStoryLibraryRepository.save(libraryEntry);

        } else {
            response.setIsFavorite(false);
        }

        return response;
    }

    @Override
    public ChapterResponse getChapterById(String id) {
        ChapterEntity chapter = chapterRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy chương"));
        return chapterMapper.toResponse(chapter);
    }

    @Override
    @Transactional
    public void favoriteStory(String storyId) {
        UserEntity currentUser = getCurrentUser();
        StoryEntity story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy truyện"));

        UserStoryLibraryEntity libraryEntry = userStoryLibraryRepository
                .findByUserIdAndStoryId(currentUser.getId(), storyId)
                .orElseGet(() -> {
                    UserStoryLibraryEntity newEntry = new UserStoryLibraryEntity();
                    newEntry.setUser(currentUser);
                    newEntry.setStory(story);
                    return newEntry;
                });

        if(libraryEntry.getIsFavorite() == null || libraryEntry.getIsFavorite() == false)
            libraryEntry.setIsFavorite(true);
        else libraryEntry.setIsFavorite(false);
        userStoryLibraryRepository.save(libraryEntry);
    }

    @Override
    @Transactional
    public void deleteStory(String storyId) {
        UserEntity currentUser = getCurrentUser();
        StoryEntity story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy truyện"));

        // Check permission
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            if (!story.getUploader().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xóa truyện này");
            }
        }

        // Delete related UserStoryLibrary entries first
        List<UserStoryLibraryEntity> libraryEntries = userStoryLibraryRepository.findAllByStoryId(storyId);
        userStoryLibraryRepository.deleteAll(libraryEntries);

        // Delete story (Cascade will handle chapters and categories)
        storyRepository.delete(story);
    }

    @Override
    @Transactional
    public void toggleWebView(String storyId) {
        StoryEntity story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy truyện"));
        
        // Toggle logic: if null or false -> true, if true -> false
        boolean currentStatus = story.getWebView() != null && story.getWebView();
        story.setWebView(!currentStatus);
        
        storyRepository.save(story);
    }

    @Override
    public long countStories() {
        return storyRepository.count();
    }

    @Override
    public List<String> getAllCategories() {
        return Arrays.stream(StoryCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        return (UserEntity) authentication.getPrincipal();
    }

    private UserEntity getCurrentUserOpt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return (UserEntity) authentication.getPrincipal();
        }
        return null;
    }
}
