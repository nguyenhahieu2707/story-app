package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.StoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<StoryEntity, String>, JpaSpecificationExecutor<StoryEntity> {
    Page<StoryEntity> findByWebViewTrue(Pageable pageable);

    Page<StoryEntity> findByUploaderId(String uploaderId, Pageable pageable);
}
