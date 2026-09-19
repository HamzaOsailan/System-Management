package com.sahab.demo.config;

import com.sahab.demo.service.WebAuthnCredentialRepositoryAdapter;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class WebAuthnConfig {

    @Value("${webauthn.rp-id:localhost}")
    private String rpId;

    @Value("${webauthn.rp-name:Sahab}")
    private String rpName;

    @Value("${webauthn.origin:http://localhost:5173}")
    private String origin;

    @Bean
    public RelyingParty relyingParty(
            WebAuthnCredentialRepositoryAdapter credentialRepository
    ) {

        RelyingPartyIdentity identity =
                RelyingPartyIdentity.builder()
                        .id(rpId)
                        .name(rpName)
                        .build();

        return RelyingParty
                .builder()
                .identity(identity)
                .credentialRepository(
                        credentialRepository
                )
                .origins(Set.of(origin))
                .validateSignatureCounter(true)
                .build();
    }
}