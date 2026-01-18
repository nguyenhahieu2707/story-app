package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, String> {
}
