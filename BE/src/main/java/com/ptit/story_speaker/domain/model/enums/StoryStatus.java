package com.ptit.story_speaker.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoryStatus {
    DRAFT("Chưa phát hành"),
    ONGOING("Đang phát hành"),
    COMPLETED("Đã hoàn thành");

    private final String description;
}
