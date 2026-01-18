package com.ptit.story_speaker.domain.mapper;

import com.ptit.story_speaker.domain.dto.response.StoryResponse;
import com.ptit.story_speaker.domain.entity.StoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ChapterMapper.class})
public interface StoryMapper {

    @Mapping(source = "uploader.name", target = "uploaderName")
    StoryResponse toStoryResponse(StoryEntity entity);
}
