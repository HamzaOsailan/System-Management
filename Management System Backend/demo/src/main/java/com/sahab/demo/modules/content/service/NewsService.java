package com.sahab.demo.modules.content.service;

import com.sahab.demo.modules.content.dto.NewsResponseDTO;
import com.sahab.demo.modules.content.entity.News;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.modules.content.repository.NewsRepository;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    /* =========================================================
       GET ALL NEWS
    ========================================================= */

    @Transactional(readOnly = true)
    public List<NewsResponseDTO> getAllNews() {

        return newsRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================================================
       GET SINGLE NEWS
    ========================================================= */

    @Transactional(readOnly = true)
    public NewsResponseDTO getNews(Long id) {

        News news = newsRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("News not found with id: " + id)
                );

        return toDTO(news);
    }

    /* =========================================================
       CREATE NEWS
       ADMIN ONLY
    ========================================================= */

    @Transactional
    public NewsResponseDTO createNews(
            String title,
            String content,
            MultipartFile image
    ) throws IOException {

        User admin = getCurrentUser();

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only administrators can create news"
            );
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "News title is required"
            );
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "News content is required"
            );
        }

        News news = new News();

        news.setTitle(title.trim());
        news.setContent(content.trim());
        news.setCreatedByName(admin.getName());

        if (image != null && !image.isEmpty()) {

            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                        "Image size must not exceed 5 MB"
                );
            }

            String contentType = image.getContentType();

            if (contentType == null ||
                    !contentType.toLowerCase().startsWith("image/")) {

                throw new IllegalArgumentException(
                        "Only image files are allowed"
                );
            }

            news.setImageData(image.getBytes());
            news.setImageContentType(contentType);
            news.setImageName(image.getOriginalFilename());
            news.setHasImage(true);
        }

        News savedNews = newsRepository.save(news);

        return toDTO(savedNews);
    }

    /* =========================================================
       GET IMAGE
    ========================================================= */

    @Transactional(readOnly = true)
    public NewsImage getImage(Long id) {

        News news = newsRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("News not found with id: " + id)
                );

        if (!news.isHasImage() ||
                news.getImageData() == null) {

            throw new RuntimeException(
                    "This news item does not have an image"
            );
        }

        return new NewsImage(
                news.getImageData(),
                news.getImageContentType()
        );
    }

    /* =========================================================
       DELETE NEWS
       ADMIN ONLY
    ========================================================= */

    @Transactional
    public void deleteNews(Long id) {

        User admin = getCurrentUser();

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only administrators can delete news"
            );
        }

        if (!newsRepository.existsById(id)) {
            throw new RuntimeException(
                    "News not found with id: " + id
            );
        }

        newsRepository.deleteById(id);
    }

    /* =========================================================
       CURRENT USER
    ========================================================= */

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null) {

            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Current user not found"
                        )
                );
    }

    /* =========================================================
       DTO MAPPING
    ========================================================= */

    private NewsResponseDTO toDTO(News news) {

        return NewsResponseDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .createdAt(news.getCreatedAt())
                .createdByName(news.getCreatedByName())
                .hasImage(news.isHasImage())
                .build();
    }

    /* =========================================================
       IMAGE RESPONSE
    ========================================================= */

    public record NewsImage(
            byte[] data,
            String contentType
    ) {
    }
}