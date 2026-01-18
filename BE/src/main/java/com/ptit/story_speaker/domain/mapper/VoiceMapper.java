package com.ptit.story_speaker.domain.mapper;

import com.ptit.story_speaker.domain.dto.response.VoiceResponse;
import com.ptit.story_speaker.domain.entity.VoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VoiceMapper {
    @Mapping(source = "modelId", target = "modelId")
    VoiceResponse toVoiceResponse(VoiceEntity entity);

    List<VoiceResponse> toVoiceResponseList(List<VoiceEntity> entities);
}
