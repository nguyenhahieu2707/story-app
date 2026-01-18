package com.ptit.story_speaker.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USER_STORY_LIBRARY")
public class UserStoryLibraryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID")
    private StoryEntity story;

    @Column(name = "IS_DOWNLOADED")
    private Boolean isDownloaded = false;

    @Column(name = "IS_COMPLETED")
    private Boolean isCompleted = false;

    @Column(name = "IS_FAVORITE")
    private Boolean isFavorite = false;

    @Column(name = "LAST_SEEN")
    private LocalDateTime lastSeen;
}
