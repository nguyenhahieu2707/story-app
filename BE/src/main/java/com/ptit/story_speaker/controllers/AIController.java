package com.ptit.story_speaker.controllers;

import com.ptit.story_speaker.domain.dto.request.StoryGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceTrainRequest;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.domain.dto.response.VoiceResponse;
import com.ptit.story_speaker.services.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "Tạo story", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/generate-story")
    public ResponseEntity<StoryResponse> generateStory(@RequestBody StoryGenerationRequest request) {
        return ResponseEntity.ok(aiService.generateStory(request));
    }

    @PostMapping("/generate-voice")
    public ResponseEntity<Map<String, String>> generateVoice(@RequestBody VoiceGenerationRequest request) {
        String audioPath = aiService.generateVoice(request);
        if (audioPath == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("audio_path", audioPath));
    }

    @PostMapping(value = "/train-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceResponse> trainModel(@ModelAttribute VoiceTrainRequest request) {
        return ResponseEntity.ok(aiService.trainModel(request));
    }

    @GetMapping("/my-voices")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<VoiceResponse>> getMyVoices() {
        return ResponseEntity.ok(aiService.getMyVoices());
    }
}
