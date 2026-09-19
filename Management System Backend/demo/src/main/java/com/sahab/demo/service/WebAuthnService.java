package com.sahab.demo.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.demo.dto.AttendanceDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.entity.WebAuthnCredential;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebAuthnService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final WebAuthnCredentialRepository credentialRepository;
    private final AttendanceService attendanceService;
    private final ObjectMapper objectMapper;

    private final Map<String, PublicKeyCredentialCreationOptions>
            registrationRequests = new ConcurrentHashMap<>();

    private final Map<String, AssertionRequest>
            assertionRequests = new ConcurrentHashMap<>();

    public WebAuthnService(
            RelyingParty relyingParty,
            UserRepository userRepository,
            WebAuthnCredentialRepository credentialRepository,
            AttendanceService attendanceService,
            ObjectMapper objectMapper
    ) {
        this.relyingParty = relyingParty;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.attendanceService = attendanceService;
        this.objectMapper = objectMapper;
    }

    // =====================================================
    // REGISTRATION START
    // =====================================================

    public JsonNode startRegistration() {

        User user = getCurrentUser();

        ByteArray userHandle = buildUserHandle(user);

        UserIdentity identity = UserIdentity.builder()
                .name(user.getEmail())
                .displayName(user.getName())
                .id(userHandle)
                .build();

        PublicKeyCredentialCreationOptions options =
                relyingParty.startRegistration(
                        StartRegistrationOptions.builder()
                                .user(identity)
                                .authenticatorSelection(
                                        AuthenticatorSelectionCriteria.builder()
                                                .authenticatorAttachment(
                                                        AuthenticatorAttachment.PLATFORM
                                                )
                                                .userVerification(
                                                        UserVerificationRequirement.REQUIRED
                                                )
                                                .build()
                                )
                                .build()
                );

        registrationRequests.put(
                user.getEmail(),
                options
        );

        try {
            return readJson(
                    options.toCredentialsCreateJson()
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create registration JSON",
                    e
            );
        }
    }

    // =====================================================
    // REGISTRATION FINISH
    // =====================================================

    public String finishRegistration(
            JsonNode credentialJson
    ) {

        User user = getCurrentUser();

        PublicKeyCredentialCreationOptions request =
                registrationRequests.remove(
                        user.getEmail()
                );

        if (request == null) {
            throw new RuntimeException(
                    "Registration request expired or not found"
            );
        }

        try {

            PublicKeyCredential<
                    AuthenticatorAttestationResponse,
                    ClientRegistrationExtensionOutputs
                    > credential =
                    PublicKeyCredential
                            .parseRegistrationResponseJson(
                                    credentialJson.toString()
                            );

            RegistrationResult result =
                    relyingParty.finishRegistration(
                            FinishRegistrationOptions.builder()
                                    .request(request)
                                    .response(credential)
                                    .build()
                    );

            WebAuthnCredential saved =
                    new WebAuthnCredential();

            saved.setUser(user);

            saved.setCredentialId(
                    result.getKeyId()
                            .getId()
                            .getBase64Url()
            );

            saved.setPublicKey(
                    result.getPublicKeyCose()
                            .getBase64Url()
            );

            saved.setUserHandle(
                    request.getUser()
                            .getId()
                            .getBase64Url()
            );

            saved.setSignatureCount(
                    result.getSignatureCount()
            );

            credentialRepository.save(saved);

            return "Biometric registration completed successfully";

        } catch (IOException e) {

            throw new RuntimeException(
                    "Invalid biometric registration response",
                    e
            );

        } catch (RegistrationFailedException e) {

            throw new RuntimeException(
                    "Biometric registration failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // ASSERTION START
    // =====================================================

    public JsonNode startAuthentication() {

        User user = getCurrentUser();

        if (credentialRepository.findByUser(user).isEmpty()) {

            throw new RuntimeException(
                    "Biometric authentication is not registered"
            );
        }

        AssertionRequest request =
                relyingParty.startAssertion(
                        StartAssertionOptions.builder()
                                .username(user.getEmail())
                                .userVerification(
                                        UserVerificationRequirement.REQUIRED
                                )
                                .build()
                );

        assertionRequests.put(
                user.getEmail(),
                request
        );

        try {

            return readJson(
                    request.toCredentialsGetJson()
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create authentication JSON",
                    e
            );
        }
    }

    // =====================================================
    // ASSERTION FINISH
    // =====================================================

    public AttendanceDTO finishAuthentication(
            JsonNode credentialJson
    ) {

        User user = getCurrentUser();

        AssertionRequest request =
                assertionRequests.remove(
                        user.getEmail()
                );

        if (request == null) {

            throw new RuntimeException(
                    "Authentication request expired or not found"
            );
        }

        try {

            PublicKeyCredential<
                    AuthenticatorAssertionResponse,
                    ClientAssertionExtensionOutputs
                    > credential =
                    PublicKeyCredential
                            .parseAssertionResponseJson(
                                    credentialJson.toString()
                            );

            var result =
                    relyingParty.finishAssertion(
                            FinishAssertionOptions.builder()
                                    .request(request)
                                    .response(credential)
                                    .build()
                    );

            if (!result.isSuccess()) {

                throw new RuntimeException(
                        "Biometric verification failed"
                );
            }

            String credentialId =
                    result.getCredential()
                            .getCredentialId()
                            .getBase64Url();

            WebAuthnCredential stored =
                    credentialRepository
                            .findByCredentialId(
                                    credentialId
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Credential not found"
                                    )
                            );

            stored.setSignatureCount(
                    result.getSignatureCount()
            );

            credentialRepository.save(stored);

            return attendanceService.checkInOrOut(user);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Invalid biometric authentication response",
                    e
            );

        } catch (AssertionFailedException e) {

            throw new RuntimeException(
                    "Biometric verification failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // STATUS
    // =====================================================

    public boolean isRegistered() {

        User user = getCurrentUser();

        return !credentialRepository
                .findByUser(user)
                .isEmpty();
    }

    // =====================================================
    // CURRENT USER
    // =====================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    // =====================================================
    // USER HANDLE
    // =====================================================

    private ByteArray buildUserHandle(User user) {

        return new ByteArray(
                (
                        "sahab-user-" +
                                user.getId()
                ).getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    // =====================================================
    // JSON
    // =====================================================

    private JsonNode readJson(String json) {

        try {

            return objectMapper.readTree(json);

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create WebAuthn JSON",
                    e
            );
        }
    }
}

