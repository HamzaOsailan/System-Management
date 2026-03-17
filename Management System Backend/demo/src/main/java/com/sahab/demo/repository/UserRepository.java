package com.sahab.demo.repository;

import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findFirstByRole(Role role);

}