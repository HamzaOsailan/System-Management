package com.sahab.demo.service;

import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getUserById(Long id) {

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    // ===============================
    // جديد: تغيير دور مستخدم
    // ===============================
    public User updateUserRole(Long id, Role newRole) {

        User user = getUserById(id);

        user.setRole(newRole);

        return userRepository.save(user);
    }

    // ===============================
    // جديد: حذف مستخدم
    // ===============================
    public void deleteUser(Long id) {

        User user = getUserById(id);

        userRepository.delete(user);
    }
}