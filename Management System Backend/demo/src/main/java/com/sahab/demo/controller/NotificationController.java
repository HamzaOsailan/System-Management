package com.sahab.demo.controller;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {

        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // ======================================================
    // GET MY NOTIFICATIONS
    // ======================================================

    @GetMapping
    public List<Notification> getMyNotifications(){

        // 🟢 استخراج المستخدم من JWT

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        return notificationService.getUserNotifications(user);
    }
}