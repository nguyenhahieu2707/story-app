package com.ptit.story_speaker.services.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.story_speaker.common.exceptions.AppException;
import com.ptit.story_speaker.common.exceptions.ErrorCode;
import com.ptit.story_speaker.domain.dto.request.CharacterInput;
import com.ptit.story_speaker.domain.dto.request.StoryGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceTrainRequest;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.domain.dto.response.VoiceResponse;
import com.ptit.story_speaker.domain.dto.response.VoiceTrainResponse;
import com.ptit.story_speaker.domain.entity.*;
import com.ptit.story_speaker.domain.mapper.StoryMapper;
import com.ptit.story_speaker.domain.mapper.VoiceMapper;
import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import com.ptit.story_speaker.repository.*;
import com.ptit.story_speaker.services.AIService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final RestClient.Builder restClientBuilder;
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    private final StoryMapper storyMapper;
    private final UserRepository userRepository;
    private final VoiceRepository voiceRepository;
    private final ObjectMapper objectMapper;
    private final VoiceMapper voiceMapper;
    private final TemporaryFileRepository temporaryFileRepository;

    // DTO for parsing AI response
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AIStoryGenerationResponse {
        private Meta meta;
        private StoryContent story;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private String title;
        private String description;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StoryContent {
        private String beginning;
        private String middle;
        private String end;
    }

    @Override
    @Transactional
    public StoryResponse generateStory(StoryGenerationRequest req) {
        String systemPrompt = "SYSTEM: Bạn là một trợ lý viết truyện dành cho trẻ em (story-speaker). \n" +
                "Quy tắc nghiêm ngặt:\n" +
                "1. Nội dung phải an toàn cho trẻ em: không bạo lực, không nội dung người lớn, không ngôn từ tục tĩu.\n" +
                "2. Luôn tuân thủ field `readingLevel` khi lựa chọn từ vựng và câu.\n" +
                "3. Trả story có ~{words_needed} từ (±10%). Nếu durationSeconds=300 thì ~600 từ. Phân bố: beginning ≈ 20% (120 words), middle ≈ 60% (360 words), end ≈ 20% (120 words). Nếu model trả ngắn hơn, hãy BỔ SUNG nhiều đoạn miêu tả, đối thoại, chi tiết cảm xúc và âm thanh cho đến đạt số từ.\n" +
                "4. Nếu user không cung cấp một field (ví dụ genre), hãy chọn default an toàn (genre: \"bedtime\", readingLevel: \"6-8\", tone: \"gentle\").\n" +
                "5. Trả RA MỘT ĐỐI TƯỢNG JSON duy nhất theo schema dưới đây, và KHÔNG đưa thêm giải thích:\n" +
                "   {\n" +
                "     \"meta\": {\n" +
                "       \"title\": \"<string>\",\n" +
                "       \"description\": \"<string>\",\n" +
                "       \"durationSeconds\": <int>,\n" +
                "       \"genre\": \"<string>\",\n" +
                "       \"readingLevel\": \"<string>\",\n" +
                "       \"tone\": \"<string>\"\n" +
                "     },\n" +
                "     \"characters\": [\n" +
                "       {\"name\":\"<string>\",\"role\":\"<string>\",\"shortDescription\":\"<string>\"}\n" +
                "     ],\n" +
                "     \"story\": {\n" +
                "       \"beginning\":\"<string>\",\n" +
                "       \"middle\":\"<string>\",\n" +
                "       \"end\":\"<string>\"\n" +
                "     },\n" +
                "     \"safety\": {\n" +
                "       \"safetyVerdict\":\"<OK|NOT_OK>\",\n" +
                "       \"reasons\":\"<string>\"\n" +
                "     }\n" +
                "   }\n" +
                "6. Nếu nội dung không an toàn, `safetyVerdict` phải là NOT_OK và `reasons` mô tả ngắn." +
                "USER:\n" +
                "Title: <title or leave blank>\n" +
                "Language: <language>\n" +
                "DurationSeconds: <durationSeconds>\n" +
                "ReadingLevel: <readingLevel>\n" +
                "Genre: <genre>\n" +
                "Tone: <tone>\n" +
                "Characters:\n" +
                " - <char1> (mô tả)\n" +
                " - <char2> (mô tả)\n" +
                "KeyMessages:\n" +
                " - <msg1>\n" +
                "AdditionalInstructions: <additionalInstructions>\n" +
                "FreeText: <freeText>";

        String userPrompt = buildPromptFromRequest(req);

        ChatClient chatClient = chatClientBuilder.build();
        String rawJsonResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // Clean up the response to get a pure JSON string
        rawJsonResponse = rawJsonResponse.replaceAll("(?s)```.*?\\n", "").replaceAll("```", "").trim();

        AIStoryGenerationResponse aiResponse;
        try {
            aiResponse = objectMapper.readValue(rawJsonResponse, AIStoryGenerationResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }

        UserEntity uploader = getCurrentUser();

        // Create and save StoryEntity
        StoryEntity newStory = new StoryEntity();
        newStory.setTitle(aiResponse.getMeta().getTitle());
        newStory.setDescription(aiResponse.getMeta().getDescription());
        newStory.setAuthor("AI Generated");
        newStory.setStatus(StoryStatus.DRAFT);
        newStory.setUploader(uploader);
        newStory.setWebView(false); // Default to not visible on web view
        StoryEntity savedStory = storyRepository.save(newStory);

        // Create and save a single ChapterEntity
        ChapterEntity chapter = new ChapterEntity();
        chapter.setStory(savedStory);
        chapter.setChapterNumber(1);
        chapter.setTitle(savedStory.getTitle()); // Use story title for the first chapter

        String fullContent = aiResponse.getStory().getBeginning() + "\n\n" +
                aiResponse.getStory().getMiddle() + "\n\n" +
                aiResponse.getStory().getEnd();
        chapter.setContent(fullContent);
        chapterRepository.save(chapter);

        // Return the full story response
        return storyMapper.toStoryResponse(savedStory);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TextToSpeechRequest {
        private String text;
        @JsonProperty("model_id")
        private String modelId;
        private String locate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TextToSpeechResponse {
        private String message;
        @JsonProperty("url")
        private String audioPath;
    }

    @Override
    public String generateVoice(VoiceGenerationRequest request) {
        var ttsRequest = new TextToSpeechRequest(request.getText(), request.getVoiceId(), request.getLanguage());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(600000);

        TextToSpeechResponse response = restClientBuilder
                .requestFactory(factory)
                .build()
                .post()
                .uri("http://localhost:8000/text-to-speech-and-infer/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ttsRequest)
                .retrieve()
                .body(TextToSpeechResponse.class);

        if (response != null && response.getAudioPath() != null) {
            // Save the generated audio URL to temporary files for cleanup
            TemporaryFileEntity tempFile = TemporaryFileEntity.builder()
                    .fileUrl(response.getAudioPath())
                    .createdAt(LocalDateTime.now())
                    .build();
            temporaryFileRepository.save(tempFile);
            
            return response.getAudioPath();
        }
        return null;
    }

    @Override
    public VoiceResponse trainModel(VoiceTrainRequest request) {
        UserEntity uploader = getCurrentUser();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                ByteArrayResource fileResource = new ByteArrayResource(request.getFile().getBytes()) {
                    @Override
                    public String getFilename() {
                        return request.getFile().getOriginalFilename();
                    }
                };
                body.add("file", fileResource);
            } else {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "File is required for training.");
            }

            body.add("name", request.getName());
            body.add("f0_method", request.getF0Method());
            body.add("epochs_number", request.getEpochsNumber());
            body.add("user_id", uploader.getId());
            body.add("trainAt", request.getTrainAt());

        } catch (java.io.IOException e) {
            throw new RuntimeException("Error reading uploaded file", e);
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(1200000);

        VoiceTrainResponse response = restClientBuilder
                .requestFactory(factory)
                .build()
                .post()
                .uri("http://localhost:8000/train-model/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(VoiceTrainResponse.class);

        if (response != null) {
            VoiceEntity voiceEntity = new VoiceEntity();
            voiceEntity.setName(request.getName());
            voiceEntity.setModelPath(response.getPath());
            voiceEntity.setModelId(response.getModel_id());
            voiceEntity.setUploader(uploader);
            VoiceEntity savedEntity = voiceRepository.save(voiceEntity);
            return voiceMapper.toVoiceResponse(savedEntity);
        } else {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to train model or parse response");
        }
    }

    @Override
    public List<VoiceResponse> getMyVoices() {
        UserEntity currentUser = getCurrentUser();
        List<VoiceEntity> voiceEntities = voiceRepository.findByUploaderId(currentUser.getId());
        return voiceMapper.toVoiceResponseList(voiceEntities);
    }

    private String buildPromptFromRequest(StoryGenerationRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(Optional.ofNullable(r.getTitle()).orElse("")).append("\n");
        sb.append("Language: ").append(Optional.ofNullable(r.getLanguage()).orElse("vi")).append("\n");
        sb.append("DurationSeconds: ").append(Optional.ofNullable(r.getDurationSeconds()).orElse(180)).append("\n");
        sb.append("ReadingLevel: ").append(Optional.ofNullable(r.getReadingLevel()).orElse("6-8")).append("\n");
        sb.append("Genre: ").append(Optional.ofNullable(r.getGenre()).orElse("bedtime")).append("\n");
        sb.append("Tone: ").append(Optional.ofNullable(r.getTone()).orElse("gentle")).append("\n");

        sb.append("Characters:\n");
        if (r.getCharacters() != null) {
            for (CharacterInput c : r.getCharacters()) {
                sb.append(" - ").append(c.getName())
                        .append(": ").append(Optional.ofNullable(c.getShortDescription()).orElse("")).append("\n");
            }
        }

        if (r.getKeyMessages() != null) {
            sb.append("KeyMessages:\n");
            r.getKeyMessages().forEach(k -> sb.append(" - ").append(k).append("\n"));
        }

        if (r.getAdditionalInstructions() != null) {
            sb.append("AdditionalInstructions: ").append(r.getAdditionalInstructions()).append("\n");
        }

        sb.append("FreeText: ").append(Optional.ofNullable(r.getFreeText()).orElse("")).append("\n");
        return sb.toString();
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        return (UserEntity) authentication.getPrincipal();
    }
}
