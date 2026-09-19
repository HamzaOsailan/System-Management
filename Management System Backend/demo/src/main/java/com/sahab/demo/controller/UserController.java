package com.sahab.demo.controller;

import com.sahab.demo.dto.UpdateRoleDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUser(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }


    // ===============================
    // جديد: تغيير دور مستخدم
    // ===============================
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleDTO dto
    ) {

        // حماية: ما نخلي الأدمن يغيّر دوره هو نفسه بالغلط
        // (سيناريو شائع يسبب مشاكل: أدمن يحوّل نفسه USER بالخطأ
        // ويفقد صلاحيته بدون رجعة سهلة)
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User target = userService.getUserById(id);

        if (target.getEmail().equals(currentEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "You cannot change your own role"
                    ));
        }

        User updated = userService.updateUserRole(id, dto.getRole());

        return ResponseEntity.ok(updated);
    }


    // ===============================
    // جديد: حذف مستخدم
    // ===============================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id
    ) {

        // نفس الحماية: ما نخلي الأدمن يحذف حسابه هو نفسه
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User target = userService.getUserById(id);

        if (target.getEmail().equals(currentEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "You cannot delete your own account"
                    ));
        }

        try {

            userService.deleteUser(id);

            return ResponseEntity.ok(
                    Map.of("message", "User deleted")
            );

        } catch (DataIntegrityViolationException e) {

            // يصير لو المستخدم عنده طلبات مرتبطة به بجدول request
            // (foreign key constraint) — نرجع رسالة مفهومة بدل 500 عام
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Cannot delete a user who has existing requests"
                    ));
        }
    }
}