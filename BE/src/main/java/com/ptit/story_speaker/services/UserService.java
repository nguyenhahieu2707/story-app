package com.ptit.story_speaker.services;

import com.ptit.story_speaker.domain.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    long countUsers();
}
