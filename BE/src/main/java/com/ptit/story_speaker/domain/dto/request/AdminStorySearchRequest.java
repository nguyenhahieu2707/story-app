package com.ptit.story_speaker.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStorySearchRequest {
    private String keyword; // Tìm theo tên truyện hoặc tên tác giả
    private String createdByRole; // "ADMIN", "USER" hoặc null/empty
}
