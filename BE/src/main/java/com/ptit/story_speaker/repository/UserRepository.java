package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.UserEntity;
import com.ptit.story_speaker.domain.model.enums.LoginType;
import com.ptit.story_speaker.domain.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findBySocialUserIdAndLoginType(String socialUserId, LoginType loginType);
    Optional<UserEntity> findByUsername(String username);
    Page<UserEntity> findByRole(Role role, Pageable pageable);
    long countByRole(Role role);
}
