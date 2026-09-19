package com.sahab.demo.controller;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.repository.NotificationRepository;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.NotificationService;

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
    private final NotificationRepository notificationRepository;

    // =========================================================
    // GET MY NOTIFICATIONS
    // GET /notifications
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        List<Notification> notifications =
                notificationService
                        .getUserNotifications(user);

        return ResponseEntity.ok(notifications);
    }
    // =========================================================
    // GET UNREAD COUNT
    // GET /notifications/unread-count
    // =========================================================

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication){
        User user =
                getCurrentUser(authentication);

        long count =
                notificationService.getUnreadCount(user);

        return ResponseEntity.ok(count);
    }

    @PutMapping("/read")
    public ResponseEntity<String> markAllAsRead(Authentication authentication){
        User user =getCurrentUser(authentication);

        List<Notification> notifications=notificationRepository.findByUser(user);

        for (Notification notification: notifications){
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok("notifications marked as read");
    }
    // =========================================================
    // GET CURRENT USER
    // =========================================================

    public User getCurrentUser(Authentication authentication){
        if (authentication ==null || !authentication.isAuthenticated()){
            throw new RuntimeException(" User is not authenticated");
        }
        String email =authentication.getName();
        return userRepository
                .findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException(
                        "User not found"
                ));


    }

}