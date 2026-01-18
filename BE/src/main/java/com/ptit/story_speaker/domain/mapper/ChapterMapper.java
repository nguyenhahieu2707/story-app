package com.ptit.story_speaker.domain.mapper;

import com.ptit.story_speaker.domain.dto.response.ChapterResponse;
import com.ptit.story_speaker.domain.entity.ChapterEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    ChapterResponse toResponse(ChapterEntity entity);
}
