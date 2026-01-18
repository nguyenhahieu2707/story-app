package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.VoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceRepository extends JpaRepository<VoiceEntity, String> {
    List<VoiceEntity> findByUploaderId(String uploaderId);
}
