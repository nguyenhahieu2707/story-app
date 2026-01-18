package com.ptit.story_speaker.services;

import com.ptit.story_speaker.domain.dto.request.StoryGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceGenerationRequest;
import com.ptit.story_speaker.domain.dto.request.VoiceTrainRequest;
import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.domain.dto.response.VoiceResponse;

import java.util.List;

public interface AIService {
    StoryResponse generateStory(StoryGenerationRequest userPrompt);
    String generateVoice(VoiceGenerationRequest request);
    VoiceResponse trainModel(VoiceTrainRequest request);
    List<VoiceResponse> getMyVoices();
}
