package com.sahab.demo.repository;

import com.sahab.demo.entity.User;
import com.sahab.demo.entity.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebAuthnCredentialRepository
        extends JpaRepository<WebAuthnCredential, Long> {

    List<WebAuthnCredential> findByUser(User user);

    Optional<WebAuthnCredential> findByCredentialId(
            String credentialId
    );

    Optional<WebAuthnCredential> findByUserHandle(
            String userHandle
    );
}