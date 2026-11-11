package com.sahab.demo.modules.notifications.controller;

import com.sahab.demo.modules.notifications.dto.NotificationDTO;
import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.modules.notifications.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;


    // =========================================================
    // GET MY NOTIFICATIONS
    // GET /notifications
    // =========================================================

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        List<Notification> notifications =
                notificationService.getUserNotifications(user);

        List<NotificationDTO> response =
                notifications.stream()
                        .map(notification ->
                                new NotificationDTO(
                                        notification.getId(),
                                        notification.getMessage(),
                                        notification.isRead(),
                                        notification.getCreatedAt()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET UNREAD COUNT
    // GET /notifications/unread-count
    // =========================================================

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        long count =
                notificationService.getUnreadCount(user);

        return ResponseEntity.ok(count);
    }


    // =========================================================
    // MARK ALL AS READ
    // PUT /notifications/read-all
    // =========================================================

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        int updated =
                notificationService.markAllAsRead(user);

        return ResponseEntity.ok(
                updated +
                        " notifications marked as read"
        );
    }


    // =========================================================
    // LEGACY MARK ALL AS READ
    // PUT /notifications/read
    //
    // Kept for backward compatibility.
    // =========================================================

    @PutMapping("/read")
    public ResponseEntity<String> markAllAsReadLegacy(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        int updated =
                notificationService.markAllAsRead(user);

        return ResponseEntity.ok(
                updated +
                        " notifications marked as read"
        );
    }


    // =========================================================
    // MARK ONE NOTIFICATION AS READ
    // PUT /notifications/{id}/read
    // =========================================================

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markNotificationAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        boolean updated =
                notificationService.markAsRead(
                        id,
                        user
                );

        if (!updated) {
            throw new ResourceNotFoundException(
                    "Notification not found"
            );
        }

        return ResponseEntity.ok(
                "notification marked as read"
        );
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                        !authentication.isAuthenticated()
        ) {
            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email =
                authentication.getName();

        if (
                email == null ||
                        email.isBlank()
        ) {
            throw new RuntimeException(
                    "Authenticated user email is missing"
            );
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }
}