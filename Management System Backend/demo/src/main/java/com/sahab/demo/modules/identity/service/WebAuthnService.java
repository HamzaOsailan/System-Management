package com.sahab.demo.modules.identity.service;
import com.sahab.demo.modules.hr.service.AttendanceService;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.demo.modules.hr.dto.AttendanceDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.entity.WebAuthnCredential;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.modules.identity.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
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

    /*
     * Stores WebAuthn registration challenges temporarily.
     *
     * Key = current user's email
     */
    private final Map<String, PublicKeyCredentialCreationOptions>
            registrationRequests = new ConcurrentHashMap<>();

    /*
     * Stores WebAuthn authentication challenges temporarily.
     *
     * Key = current user's email
     */
    private final Map<String, AssertionRequest>
            assertionRequests = new ConcurrentHashMap<>();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

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


    // =========================================================
    // REGISTRATION START
    //
    // POST:
    // /attendance/webauthn/register/start
    // =========================================================

    public JsonNode startRegistration() {

        User user = getCurrentUser();

        /*
         * Create a stable WebAuthn user handle.
         *
         * Example:
         * sahab-user-23
         */
        ByteArray userHandle =
                buildUserHandle(user);

        UserIdentity identity =
                UserIdentity.builder()
                        .name(user.getEmail())
                        .displayName(user.getName())
                        .id(userHandle)
                        .build();

        /*
         * Ask Yubico WebAuthn library to create
         * the registration challenge/options.
         */
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

        /*
         * Save the request temporarily.
         *
         * The same user must finish the registration
         * using this request.
         */
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


    // =========================================================
    // REGISTRATION FINISH
    //
    // POST:
    // /attendance/webauthn/register/finish
    // =========================================================

    public String finishRegistration(
            JsonNode credentialJson
    ) {

        User user = getCurrentUser();

        /*
         * Retrieve the registration request
         * created by startRegistration().
         */
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

            /*
             * Convert browser JSON into Yubico
             * WebAuthn credential object.
             */
            PublicKeyCredential<
                    AuthenticatorAttestationResponse,
                    ClientRegistrationExtensionOutputs
                    > credential =
                    PublicKeyCredential
                            .parseRegistrationResponseJson(
                                    credentialJson.toString()
                            );

            /*
             * Verify registration response.
             */
            RegistrationResult result =
                    relyingParty.finishRegistration(
                            FinishRegistrationOptions.builder()
                                    .request(request)
                                    .response(credential)
                                    .build()
                    );

            /*
             * Save credential in database.
             */
            WebAuthnCredential saved =
                    new WebAuthnCredential();

            saved.setUser(user);

            /*
             * Credential ID.
             */
            saved.setCredentialId(
                    result.getKeyId()
                            .getId()
                            .getBase64Url()
            );

            /*
             * Public key.
             */
            saved.setPublicKey(
                    result.getPublicKeyCose()
                            .getBase64Url()
            );

            /*
             * User handle.
             */
            saved.setUserHandle(
                    request.getUser()
                            .getId()
                            .getBase64Url()
            );

            /*
             * Signature counter.
             */
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


    // =========================================================
    // AUTHENTICATION START
    //
    // POST:
    // /attendance/webauthn/authenticate/start
    // =========================================================

    public JsonNode startAuthentication() {

        User user = getCurrentUser();

        /*
         * User must have at least one registered
         * biometric credential.
         */
        if (
                credentialRepository
                        .findByUser(user)
                        .isEmpty()
        ) {

            throw new RuntimeException(
                    "Biometric authentication is not registered"
            );
        }

        /*
         * Create WebAuthn authentication challenge.
         */
        AssertionRequest request =
                relyingParty.startAssertion(
                        StartAssertionOptions.builder()
                                .username(user.getEmail())
                                .userVerification(
                                        UserVerificationRequirement.REQUIRED
                                )
                                .build()
                );

        /*
         * Save challenge temporarily.
         */
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


    // =========================================================
    // AUTHENTICATION FINISH
    //
    // POST:
    // /attendance/webauthn/authenticate/finish
    // =========================================================

    public AttendanceDTO finishAuthentication(
            JsonNode credentialJson
    ) {

        User user = getCurrentUser();

        /*
         * Retrieve authentication challenge.
         */
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

            /*
             * Convert browser JSON to Yubico credential.
             */
            PublicKeyCredential<
                    AuthenticatorAssertionResponse,
                    ClientAssertionExtensionOutputs
                    > credential =
                    PublicKeyCredential
                            .parseAssertionResponseJson(
                                    credentialJson.toString()
                            );

            /*
             * Verify biometric assertion.
             */
            var result =
                    relyingParty.finishAssertion(
                            FinishAssertionOptions.builder()
                                    .request(request)
                                    .response(credential)
                                    .build()
                    );

            /*
             * Verification failed.
             */
            if (!result.isSuccess()) {

                throw new RuntimeException(
                        "Biometric verification failed"
                );
            }

            /*
             * Get credential ID returned by WebAuthn.
             */
            String credentialId =
                    result.getCredential()
                            .getCredentialId()
                            .getBase64Url();

            /*
             * Find credential in our database.
             */
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

            /*
             * Update signature counter.
             */
            stored.setSignatureCount(
                    result.getSignatureCount()
            );

            credentialRepository.save(stored);

            /*
             * Biometric verification succeeded.
             *
             * Now execute the existing attendance logic.
             */
            return attendanceService.checkInOrOut(
                    user
            );

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


    // =========================================================
    // STATUS
    //
    // GET:
    // /attendance/webauthn/status
    // =========================================================

    public boolean isRegistered() {

        User user = getCurrentUser();

        return !credentialRepository
                .findByUser(user)
                .isEmpty();
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        authentication.getName() == null ||
                        authentication.getName().isBlank()
        ) {

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


    // =========================================================
    // USER HANDLE
    // =========================================================

    private ByteArray buildUserHandle(
            User user
    ) {

        return new ByteArray(
                (
                        "sahab-user-" +
                                user.getId()
                ).getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }


    // =========================================================
    // JSON
    // =========================================================

    private JsonNode readJson(
            String json
    ) {

        try {

            return objectMapper.readTree(
                    json
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create WebAuthn JSON",
                    e
            );
        }
    }
}