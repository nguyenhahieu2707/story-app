package com.ptit.story_speaker.services.impl;

import com.ptit.story_speaker.domain.dto.response.ChapterExtractionResponse;
import com.ptit.story_speaker.domain.dto.response.StoryExtractionResponse;
import com.ptit.story_speaker.domain.entity.TemporaryFileEntity;
import com.ptit.story_speaker.repository.TemporaryFileRepository;
import com.ptit.story_speaker.services.EpubService;
import com.ptit.story_speaker.services.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpubServiceImpl implements EpubService {

    private final MinioService minioService;
    private final TemporaryFileRepository temporaryFileRepository;

    @Override
    public StoryExtractionResponse extractStoryFromEpub(MultipartFile epubFile) throws IOException {
        try (InputStream epubInputStream = epubFile.getInputStream()) {
            Book book = (new EpubReader()).readEpub(epubInputStream);
            StoryExtractionResponse response = new StoryExtractionResponse();

            // 1. Extract metadata
            if (book.getMetadata().getTitles() != null && !book.getMetadata().getTitles().isEmpty()) {
                response.setTitle(book.getMetadata().getTitles().get(0));
            }
            if (book.getMetadata().getAuthors() != null && !book.getMetadata().getAuthors().isEmpty()) {
                response.setAuthor(book.getMetadata().getAuthors().get(0).getFirstname() + " " + book.getMetadata().getAuthors().get(0).getLastname());
            }
            if (book.getMetadata().getDescriptions() != null && !book.getMetadata().getDescriptions().isEmpty()) {
                response.setDescription(Jsoup.parse(book.getMetadata().getDescriptions().get(0)).text());
            }

            // 2. Upload cover image to MinIO
            if (book.getCoverImage() != null) {
                Resource coverImageResource = book.getCoverImage();
                String extension = getExtension(coverImageResource.getMediaType().getName());
                String coverImageFileName = "cover-" + UUID.randomUUID() + extension;
                String coverImageUrl = minioService.uploadFile(coverImageResource.getInputStream(), coverImageFileName, coverImageResource.getMediaType().getName());
                
                // Save to temporary files
                saveToTemporaryFiles(coverImageUrl);
                
                response.setCoverImageUrl(coverImageUrl);
            }

            // 3. Extract chapters and process content images
            List<ChapterExtractionResponse> chapters = new ArrayList<>();
            Spine spine = book.getSpine();

            // Handle single chapter case vs multiple chapters
            if (spine.size() > 0) {
                for (int i = 0; i < spine.size(); i++) {
                    SpineReference spineReference = spine.getSpineReferences().get(i);
                    Resource chapterResource = spineReference.getResource();
                    
                    // Get the href of the current chapter (e.g., "Text/chapter1.html") to resolve relative image paths
                    String chapterHref = chapterResource.getHref();
                    
                    String chapterContent = new String(chapterResource.getData());
                    
                    // Process HTML to upload images and replace links
                    String processedContent = processChapterContent(chapterContent, chapterHref, book);

                    Document doc = Jsoup.parse(processedContent);
                    ChapterExtractionResponse chapter = new ChapterExtractionResponse();
                    
                    String title = doc.title();
                    if (title == null || title.isEmpty()) {
                        // Try to find h1 or h2 as title if doc title is missing
                        Element h1 = doc.selectFirst("h1");
                        if (h1 != null) {
                            title = h1.text();
                        } else {
                            title = (spine.size() == 1 && response.getTitle() != null) ? response.getTitle() : "Chapter " + (i + 1);
                        }
                    }
                    
                    chapter.setTitle(title);
                    chapter.setChapterNumber(i + 1);
                    chapter.setContent(doc.body().html()); // Get only body content
                    chapters.add(chapter);
                }
            }
            response.setChapters(chapters);

            return response;
        }
    }

    private String processChapterContent(String htmlContent, String chapterHref, Book book) {
        Document doc = Jsoup.parse(htmlContent);

        // 1. Handle standard HTML <img> tags
        for (Element img : doc.select("img")) {
            processImageElement(img, "src", chapterHref, book);
        }

        // 2. Handle SVG <image> tags (often used for covers or full page images in EPUB)
        // Example: <image width="675" height="968" xlink:href="../images/credit.jpg" />
        for (Element svgImg : doc.select("image")) {
            // SVG images usually use xlink:href, but sometimes just href
            String attrName = svgImg.hasAttr("xlink:href") ? "xlink:href" : "href";
            processImageElement(svgImg, attrName, chapterHref, book);
        }

        return doc.outerHtml();
    }

    private void processImageElement(Element element, String attrName, String chapterHref, Book book) {
        String src = element.attr(attrName);

        // Skip if src is empty, absolute URL, or base64
        if (src == null || src.isEmpty() || src.startsWith("http") || src.startsWith("data:")) {
            return;
        }

        try {
            // Resolve relative path (e.g., "../images/pic.jpg" relative to "Text/chap1.html")
            String imageHref = resolveHref(chapterHref, src);

            Resource imageResource = book.getResources().getByHref(imageHref);
            if (imageResource != null) {
                String contentType = imageResource.getMediaType().getName();
                String extension = getExtension(contentType);
                String fileName = "story-img-" + UUID.randomUUID() + extension;

                // Upload to MinIO
                String minioUrl = minioService.uploadFile(imageResource.getInputStream(), fileName, contentType);
                
                // Save to temporary files
                saveToTemporaryFiles(minioUrl);

                // Replace attribute with MinIO URL
                element.attr(attrName, minioUrl);
            } else {
                log.warn("Could not find image resource in EPUB: " + imageHref);
            }
        } catch (Exception e) {
            log.error("Error processing image in EPUB: " + src, e);
        }
    }

    private void saveToTemporaryFiles(String fileUrl) {
        TemporaryFileEntity tempFile = TemporaryFileEntity.builder()
                .fileUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .build();
        temporaryFileRepository.save(tempFile);
    }

    // Helper to resolve relative paths within the EPUB structure
    private String resolveHref(String baseHref, String relativeHref) {
        try {
            // EPUB hrefs are URI-like paths. We can use URI class to resolve.
            // We need a dummy base because URI resolution expects a scheme or absolute path for correct ".." handling sometimes,
            // but usually relative resolution works fine if we treat the base as a path.
            
            String basePath = "";
            int lastSlashIdx = baseHref.lastIndexOf('/');
            if (lastSlashIdx >= 0) {
                basePath = baseHref.substring(0, lastSlashIdx + 1);
            }
            
            URI baseUri = new URI("file:///" + basePath); // Dummy scheme to make it absolute for robust resolution
            URI resolvedUri = baseUri.resolve(relativeHref);
            
            // Remove the dummy scheme
            String resolvedPath = resolvedUri.getPath();
            if (resolvedPath.startsWith("/")) {
                resolvedPath = resolvedPath.substring(1);
            }
            
            return resolvedPath;
        } catch (URISyntaxException e) {
            return relativeHref; // Fallback
        }
    }

    private String getExtension(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
