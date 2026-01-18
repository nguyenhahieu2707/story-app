package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.RefreshTokenEntity;
import com.ptit.story_speaker.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByUser(UserEntity user);

    @Modifying
    int deleteByUser(UserEntity user);

    void deleteByToken(String token);
}
