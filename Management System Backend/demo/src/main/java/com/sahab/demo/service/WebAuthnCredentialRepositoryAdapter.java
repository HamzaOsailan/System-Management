
        package com.sahab.demo.service;

import com.sahab.demo.entity.WebAuthnCredential;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebAuthnCredentialRepositoryAdapter
        implements CredentialRepository {

    private final WebAuthnCredentialRepository repository;
    private final UserRepository userRepository;

    public WebAuthnCredentialRepositoryAdapter(
            WebAuthnCredentialRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(
            String username
    ) {
        return userRepository.findByEmail(username)
                .map(repository::findByUser)
                .orElseGet(List::of)
                .stream()
                .map(this::toDescriptor)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(
            String username
    ) {
        return userRepository.findByEmail(username)
                .flatMap(user ->
                        repository.findByUser(user)
                                .stream()
                                .findFirst()
                )
                .map(credential ->
                        decode(credential.getUserHandle())
                );
    }

    @Override
    public Optional<String> getUsernameForUserHandle(
            ByteArray userHandle
    ) {
        return repository
                .findByUserHandle(
                        userHandle.getBase64Url()
                )
                .map(credential ->
                        credential.getUser().getEmail()
                );
    }

    @Override
    public Optional<RegisteredCredential> lookup(
            ByteArray credentialId,
            ByteArray userHandle
    ) {
        return repository
                .findByCredentialId(
                        credentialId.getBase64Url()
                )
                .filter(credential ->
                        credential.getUserHandle()
                                .equals(
                                        userHandle.getBase64Url()
                                )
                )
                .map(this::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(
            ByteArray credentialId
    ) {
        return repository
                .findByCredentialId(
                        credentialId.getBase64Url()
                )
                .map(credential ->
                        Set.of(
                                toRegisteredCredential(credential)
                        )
                )
                .orElseGet(HashSet::new);
    }

    private PublicKeyCredentialDescriptor toDescriptor(
            WebAuthnCredential credential
    ) {
        return PublicKeyCredentialDescriptor.builder()
                .id(
                        decode(
                                credential.getCredentialId()
                        )
                )
                .type(
                        PublicKeyCredentialType.PUBLIC_KEY
                )
                .build();
    }

    private RegisteredCredential toRegisteredCredential(
            WebAuthnCredential credential
    ) {
        return RegisteredCredential.builder()
                .credentialId(
                        decode(
                                credential.getCredentialId()
                        )
                )
                .userHandle(
                        decode(
                                credential.getUserHandle()
                        )
                )
                .publicKeyCose(
                        decode(
                                credential.getPublicKey()
                        )
                )
                .signatureCount(
                        credential.getSignatureCount()
                )
                .build();
    }

    private ByteArray decode(String value) {
        try {
            return ByteArray.fromBase64Url(value);
        } catch (Base64UrlException e) {
            throw new IllegalStateException(
                    "Invalid WebAuthn Base64Url value",
                    e
            );
        }
    }
}
