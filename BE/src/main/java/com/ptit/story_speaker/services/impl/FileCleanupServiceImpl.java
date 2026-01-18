package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.domain.entity.TemporaryFileEntity;
import com.ptit.story_speaker.repository.TemporaryFileRepository;
import com.ptit.story_speaker.services.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileCleanupServiceImpl {

    private final TemporaryFileRepository temporaryFileRepository;
    private final MinioService minioService;

    @Scheduled(cron = "0 13 23 * * ?")
    @Transactional
    public void cleanupOrphanedFiles() {
        log.info("Starting orphaned files cleanup job...");

        LocalDateTime expiryTime = LocalDateTime.now().minusHours(24);
        List<TemporaryFileEntity> orphanedFiles = temporaryFileRepository.findByCreatedAtBefore(expiryTime);

        for (TemporaryFileEntity file : orphanedFiles) {
            try {

                minioService.removeFile(file.getFileUrl()); 

                temporaryFileRepository.delete(file);
            } catch (Exception e) {
                log.error("Failed to delete file: {}", file.getFileUrl(), e);
            }
        }
        
        log.info("Cleanup job finished. Processed {} files.", orphanedFiles.size());
    }
}
