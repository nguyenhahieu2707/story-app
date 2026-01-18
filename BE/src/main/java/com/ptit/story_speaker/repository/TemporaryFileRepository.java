package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.TemporaryFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TemporaryFileRepository extends JpaRepository<TemporaryFileEntity, String> {

    List<TemporaryFileEntity> findByCreatedAtBefore(LocalDateTime expiryDate);

    void deleteByFileUrl(String fileUrl);
}
