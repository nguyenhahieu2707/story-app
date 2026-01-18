package com.ptit.story_speaker.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterUpdateRequest {

    private String id; // Nếu có ID -> Update, nếu null -> Tạo mới

    private String title;

    private Integer chapterNumber;

    private String content;

    // Danh sách ảnh minh họa mới (nếu muốn thêm ảnh)
    private List<MultipartFile> illustrationImages;
}
