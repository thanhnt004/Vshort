package org.example.presentation.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.example.infrastructure.config.RsaKeyConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TokenController {
    private final RsaKeyConfig rsaKeyConfig;
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwksEndpoint() {
        RSAKey jwk = new RSAKey.Builder(rsaKeyConfig.publicKey())
                .keyID(rsaKeyConfig.keyId())
                .build();
        return new JWKSet(jwk).toJSONObject();
    }
}
