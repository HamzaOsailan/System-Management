
package com.sahab.demo.modules.requests.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import com.sahab.demo.exception.IdempotencyConflictException;

@Service
public class IdempotencyService {

    private final ObjectMapper objectMapper;

    public IdempotencyService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String createRequestHash(
            CreateRequestDTO dto,
            MultipartFile file
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] dtoBytes =
                    objectMapper.writeValueAsBytes(dto);

            updateDigest(digest, dtoBytes);

            if (file != null && !file.isEmpty()) {
                updateDigest(digest, file.getBytes());
            } else {
                updateDigest(
                        digest,
                        "NO_FILE".getBytes(StandardCharsets.UTF_8)
                );
            }

            return HexFormat.of().formatHex(digest.digest());

        } catch (NoSuchAlgorithmException
                 | JsonProcessingException e) {

            throw new IllegalStateException(
                    "Failed to calculate request fingerprint",
                    e
            );

        } catch (java.io.IOException e) {

            throw new IllegalStateException(
                    "Failed to read request attachment",
                    e
            );
        }
    }

    private void updateDigest(
            MessageDigest digest,
            byte[] data
    ) {
        digest.update(
                (byte) (data.length >>> 24)
        );
        digest.update(
                (byte) (data.length >>> 16)
        );
        digest.update(
                (byte) (data.length >>> 8)
        );
        digest.update(
                (byte) data.length
        );
        digest.update(data);
    }

    public String serializeResponse(
            RequestResponse response
    ) {
        try {
            return objectMapper.writeValueAsString(response);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize request response",
                    e
            );
        }
    }

    public RequestResponse deserializeResponse(
            String responseJson
    ) {
        try {
            return objectMapper.readValue(
                    responseJson,
                    RequestResponse.class
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to restore request response",
                    e
            );
        }
    }

    public void validateSameRequest(
            String existingHash,
            String incomingHash
    ) {
        if (!existingHash.equals(incomingHash)) {
            throw new IdempotencyConflictException(
                    "This Idempotency-Key was already used with different request data."
            );
        }
    }

}
