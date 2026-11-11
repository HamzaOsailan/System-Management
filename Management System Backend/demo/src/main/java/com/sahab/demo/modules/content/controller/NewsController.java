
        package com.sahab.demo.modules.content.controller;

import com.sahab.demo.modules.content.dto.NewsResponseDTO;
import com.sahab.demo.modules.content.service.NewsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NewsController {

    private final NewsService newsService;

    /* =========================================================
       GET ALL NEWS
       USER + ADMIN + MANAGER + HR
    ========================================================= */

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<NewsResponseDTO>> getAllNews() {

        return ResponseEntity.ok(
                newsService.getAllNews()
        );
    }

    /* =========================================================
       GET SINGLE NEWS
       USER + ADMIN + MANAGER + HR
    ========================================================= */

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<NewsResponseDTO> getNews(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                newsService.getNews(id)
        );
    }

    /* =========================================================
       GET NEWS IMAGE
       USER + ADMIN + MANAGER + HR
    ========================================================= */

    @GetMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id
    ) {

        NewsService.NewsImage image =
                newsService.getImage(id);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
                    image.contentType()
            );
        } catch (Exception e) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(image.data());
    }

    /* =========================================================
       CREATE NEWS
       ADMIN ONLY
    ========================================================= */

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsResponseDTO> createNews(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(
                    value = "image",
                    required = false
            ) MultipartFile image
    ) throws Exception {

        return ResponseEntity.ok(
                newsService.createNews(
                        title,
                        content,
                        image
                )
        );
    }

    /* =========================================================
       DELETE NEWS
       ADMIN ONLY
    ========================================================= */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNews(
            @PathVariable Long id
    ) {

        newsService.deleteNews(id);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "News deleted successfully"
                )
        );
    }
}
