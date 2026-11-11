package com.sahab.demo.modules.requests.controller;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.RequestAttachment;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.Role;
import com.sahab.demo.modules.requests.repository.RequestAttachmentRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/requests")
public class RequestAttachmentController {

    private final RequestAttachmentRepository requestAttachmentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestAttachmentController(
            RequestAttachmentRepository requestAttachmentRepository,
            RequestRepository requestRepository,
            UserRepository userRepository
    ) {
        this.requestAttachmentRepository =
                requestAttachmentRepository;

        this.requestRepository =
                requestRepository;

        this.userRepository =
                userRepository;
    }

    // =========================================================
    // GET REQUEST ATTACHMENT
    // =========================================================

    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> getAttachment(
            @PathVariable Long id
    ) {

        // =====================================================
        // GET REQUEST
        // =====================================================

        Request request =
                requestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Request not found"
                                )
                        );

        // =====================================================
        // GET CURRENT USER
        // =====================================================

        User currentUser =
                getCurrentUser();

        // =====================================================
        // AUTHORIZATION
        // =====================================================

        validateAttachmentAccess(
                request,
                currentUser
        );

        // =====================================================
        // GET ATTACHMENT
        // =====================================================

        RequestAttachment attachment =
                requestAttachmentRepository
                        .findByRequestId(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Attachment not found for request: " + id
                                )
                        );

        // =====================================================
        // FILE PATH
        // =====================================================

        Path filePath =
                Paths.get(
                                attachment.getFilePath()
                        )
                        .toAbsolutePath()
                        .normalize();

        // =====================================================
        // FILE EXISTS
        // =====================================================

        if (!Files.exists(filePath)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Attachment file does not exist"
            );
        }

        // =====================================================
        // FILE READABLE
        // =====================================================

        if (!Files.isReadable(filePath)) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Attachment file is not readable"
            );
        }

        try {

            // =================================================
            // RESOURCE
            // =================================================

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not read attachment file"
                );
            }

            // =================================================
            // MEDIA TYPE
            // =================================================

            MediaType mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;

            if (
                    attachment.getContentType() != null
                            &&
                            !attachment.getContentType().isBlank()
            ) {

                try {

                    mediaType =
                            MediaType.parseMediaType(
                                    attachment.getContentType()
                            );

                } catch (Exception ignored) {

                    // Keep application/octet-stream
                }
            }

            // =================================================
            // CONTENT DISPOSITION
            // =================================================

            ContentDisposition contentDisposition =
                    ContentDisposition
                            .inline()
                            .filename(
                                    attachment.getOriginalName()
                            )
                            .build();

            // =================================================
            // RESPONSE
            // =================================================

            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .contentLength(
                            Files.size(filePath)
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            contentDisposition.toString()
                    )
                    .body(resource);

        } catch (MalformedURLException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid attachment file path",
                    e
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load attachment",
                    e
            );
        }
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
        ) {

            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        String email =
                authentication.getName();

        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            throw new AccessDeniedException(
                    "User identity not found"
            );
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // ATTACHMENT ACCESS SECURITY
    // =========================================================

    private void validateAttachmentAccess(
            Request request,
            User currentUser
    ) {

        if (request == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Request not found"
            );
        }

        if (currentUser == null) {

            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // ADMIN
        // =====================================================

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // =====================================================
        // REQUEST OWNER
        // USER CAN VIEW OWN ATTACHMENT
        // =====================================================

        if (
                request.getUser() != null
                        &&
                        request.getUser()
                                .getId()
                                .equals(currentUser.getId())
        ) {
            return;
        }

        // =====================================================
        // MANAGER
        // ONLY ASSIGNED MANAGER
        // =====================================================

        if (currentUser.getRole() == Role.MANAGER) {

            if (
                    request.getManager() != null
                            &&
                            request.getManager()
                                    .getId()
                                    .equals(currentUser.getId())
            ) {
                return;
            }
        }

        // =====================================================
        // DENIED
        // =====================================================

        throw new AccessDeniedException(
                "You are not allowed to access this attachment"
        );
    }
}