package com.ptit.story_speaker.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "VOICE")
public class VoiceEntity extends BaseEntity {

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "MODEL_PATH", nullable = false)
    private String modelPath;

    @Column(name = "MODEL_ID", nullable = false, unique = true)
    private String modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPLOADER_ID")
    private UserEntity uploader;
}
