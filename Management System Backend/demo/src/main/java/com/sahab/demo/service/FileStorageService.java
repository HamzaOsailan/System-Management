        package com.sahab.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "doc",
                    "docx",
                    "jpg",
                    "jpeg",
                    "png"
            );

    private final Path uploadDirectory;

    public FileStorageService(
            @Value("${file.storage.path:uploads}") String uploadPath
    ) {
        try {
            this.uploadDirectory =
                    Paths.get(uploadPath)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadDirectory);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }

    public StoredFile store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "File is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    "Maximum file size is 5 MB"
            );
        }

        String originalName =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "file"
                                : file.getOriginalFilename()
                );

        String extension =
                getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException(
                    "File type is not allowed"
            );
        }

        String storedName =
                UUID.randomUUID()
                        + "."
                        + extension;

        Path target =
                uploadDirectory.resolve(
                        storedName
                ).normalize();

        if (!target.getParent()
                .equals(uploadDirectory)) {
            throw new RuntimeException(
                    "Invalid file path"
            );
        }

        try {

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return new StoredFile(
                    originalName,
                    storedName,
                    file.getContentType(),
                    file.getSize(),
                    target.toString()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to store file",
                    e
            );
        }
    }

    public void delete(String filePath) {

        try {
            Files.deleteIfExists(
                    Paths.get(filePath)
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete file",
                    e
            );
        }
    }

    private String getExtension(
            String filename
    ) {

        int index =
                filename.lastIndexOf('.');

        if (index == -1) {
            return "";
        }

        return filename
                .substring(index + 1)
                .toLowerCase();
    }

    public record StoredFile(
            String originalName,
            String storedName,
            String contentType,
            long fileSize,
            String filePath
    ) {}
}

