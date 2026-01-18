package com.ptit.story_speaker.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VoiceTrainResponse {
    private String name;

    @JsonProperty("minio_model_url")
    private String path;

    @JsonProperty("model_id_for_infer")
    private String model_id;
}
