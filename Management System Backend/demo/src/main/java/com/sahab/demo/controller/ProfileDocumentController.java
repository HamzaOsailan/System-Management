
        package com.sahab.demo.controller;

import com.sahab.demo.entity.User;
import com.sahab.demo.entity.UserDocument;
import com.sahab.demo.enums.DocumentType;
import com.sahab.demo.repository.UserDocumentRepository;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/profile/documents")
public class ProfileDocumentController {

    private final UserRepository userRepository;
    private final UserDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public ProfileDocumentController(
            UserRepository userRepository,
            UserDocumentRepository documentRepository,
            FileStorageService fileStorageService
    ) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<UserDocument> getMyDocuments() {

        User user = getCurrentUser();

        return documentRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    @PostMapping(
            value = "/cv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserDocument uploadCV(
            @RequestParam("file") MultipartFile file
    ) {

        return saveDocument(
                file,
                DocumentType.CV
        );
    }

    @PostMapping(
            value = "/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserDocument uploadAttachment(
            @RequestParam("file") MultipartFile file
    ) {

        return saveDocument(
                file,
                DocumentType.ATTACHMENT
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id
    ) {

        User user = getCurrentUser();

        UserDocument document =
                documentRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                )
                        );

        try {

            Path path =
                    Path.of(
                            document.getFilePath()
                    );

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists()) {
                throw new RuntimeException(
                        "File not found"
                );
            }

            String contentType =
                    document.getContentType();

            MediaType mediaType =
                    contentType != null
                            ? MediaType.parseMediaType(
                            contentType
                    )
                            : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition
                                    .attachment()
                                    .filename(
                                            document.getOriginalName()
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(resource);

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Could not read file",
                    e
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        User user = getCurrentUser();

        UserDocument document =
                documentRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                )
                        );

        fileStorageService.delete(
                document.getFilePath()
        );

        documentRepository.delete(
                document
        );

        return ResponseEntity.noContent()
                .build();
    }

    private UserDocument saveDocument(
            MultipartFile file,
            DocumentType type
    ) {

        User user = getCurrentUser();

        // Keep only the latest CV
        if (type == DocumentType.CV) {

            documentRepository
                    .findFirstByUserAndTypeOrderByCreatedAtDesc(
                            user,
                            DocumentType.CV
                    )
                    .ifPresent(oldCV -> {

                        fileStorageService.delete(
                                oldCV.getFilePath()
                        );

                        documentRepository.delete(
                                oldCV
                        );
                    });
        }

        FileStorageService.StoredFile stored =
                fileStorageService.store(file);

        UserDocument document =
                new UserDocument();

        document.setUser(user);
        document.setType(type);
        document.setOriginalName(
                stored.originalName()
        );
        document.setStoredName(
                stored.storedName()
        );
        document.setContentType(
                stored.contentType()
        );
        document.setFileSize(
                stored.fileSize()
        );
        document.setFilePath(
                stored.filePath()
        );

        return documentRepository.save(
                document
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}

