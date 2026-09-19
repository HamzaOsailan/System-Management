package com.sahab.demo.repository;

import com.sahab.demo.entity.User;
import com.sahab.demo.entity.UserDocument;
import com.sahab.demo.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDocumentRepository
        extends JpaRepository<UserDocument, Long> {

    List<UserDocument> findByUserOrderByCreatedAtDesc(User user);

    Optional<UserDocument> findByIdAndUser(Long id, User user);

    Optional<UserDocument> findFirstByUserAndTypeOrderByCreatedAtDesc(
            User user,
            DocumentType type
    );
}

