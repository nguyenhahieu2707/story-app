package com.ptit.story_speaker.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoryCategory {
    THE_THAO("Thể thao"),
    VO_THUAT("Võ thuật"),
    HAI_HUOC("Hài hước"),
    KHOA_HOC_VIEN_TUONG("Khoa học viễn tưởng"),
    CO_TICH("Cổ tích"),
    KHOA_HOC("Khoa học"),
    GIA_TUONG("Giả tưởng");

    private final String description;
}
