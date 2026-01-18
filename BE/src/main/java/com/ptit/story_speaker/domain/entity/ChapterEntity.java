package com.ptit.story_speaker.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CHAPTER")
public class ChapterEntity extends BaseEntity {

    @Column(name = "CHAPTER_NUMBER")
    private Integer chapterNumber;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORY_ID", nullable = false)
    private StoryEntity story;
}
