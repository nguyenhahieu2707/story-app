package com.ptit.story_speaker.domain.mapper;

import com.ptit.story_speaker.domain.dto.response.UserResponse;
import com.ptit.story_speaker.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(entity.getRole() != null ? entity.getRole().name() : null)")
    UserResponse toUserResponse(UserEntity entity);
}
