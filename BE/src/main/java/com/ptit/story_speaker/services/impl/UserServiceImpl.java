package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.domain.dto.response.UserResponse;
import com.ptit.story_speaker.domain.entity.UserEntity;
import com.ptit.story_speaker.domain.mapper.UserMapper;
import com.ptit.story_speaker.domain.model.enums.Role;
import com.ptit.story_speaker.repository.UserRepository;
import com.ptit.story_speaker.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // Chỉ lấy những user có Role là USER (loại bỏ ADMIN)
        Page<UserEntity> userPage = userRepository.findByRole(Role.USER, pageable);
        return userPage.map(userMapper::toUserResponse);
    }

    @Override
    public long countUsers() {
        return userRepository.countByRole(Role.USER);
    }
}
