package com.ptit.story_speaker.repository;

import com.ptit.story_speaker.domain.entity.UserStoryLibraryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserStoryLibraryRepository extends JpaRepository<UserStoryLibraryEntity, String> {

    @Query("SELECT usl.story.id FROM UserStoryLibraryEntity usl " +
            "WHERE usl.user.id = :userId " +
            "AND usl.isFavorite = true " +
            "AND usl.story.id IN :storyIds")
    Set<String> findFavoriteStoryIdsByUserIdAndStoryIds(@Param("userId") String userId,
                                                        @Param("storyIds") List<String> storyIds);

    boolean existsByUserIdAndStoryIdAndIsFavoriteTrue(String userId, String storyId);

    // Changed from LastSeenTrue to LastSeenNotNull and OrderByLastSeenDesc
    Page<UserStoryLibraryEntity> findByUserIdAndLastSeenNotNullOrderByLastSeenDesc(String userId, Pageable pageable);

    Page<UserStoryLibraryEntity> findByUserIdAndIsFavoriteTrueOrderByLastUpdateDateDesc(String userId, Pageable pageable);

    Optional<UserStoryLibraryEntity> findByUserIdAndStoryId(String id, String storyId);

    List<UserStoryLibraryEntity> findAllByStoryId(String storyId);
}
