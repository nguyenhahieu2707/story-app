package com.ptit.story_speaker.domain.entity;

import com.ptit.story_speaker.domain.model.enums.StoryCategory;
import com.ptit.story_speaker.domain.model.enums.StoryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "STORY")
public class StoryEntity extends BaseEntity {

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "AUTHOR")
    private String author;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "COVER_IMAGE_URL")
    private String coverImageUrl;

    @Column(name = "AGE_RATING")
    private Integer ageRating;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private StoryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPLOADER_ID")
    private UserEntity uploader;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterNumber ASC")
    private List<ChapterEntity> chapters;

    @ElementCollection(targetClass = StoryCategory.class)
    @CollectionTable(name = "STORY_CATEGORIES", joinColumns = @JoinColumn(name = "STORY_ID"))
    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY")
    private List<StoryCategory> categories;

    @OneToMany(mappedBy = "story")
    private List<UserStoryLibraryEntity> userStoryLibraries;
}
