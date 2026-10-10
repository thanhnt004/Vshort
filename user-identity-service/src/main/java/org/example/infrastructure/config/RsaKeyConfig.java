package org.example.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class RsaKeyConfig {

    private KeyPair keyPair;
    private final String keyId = "s-jwt-key";

    public RsaKeyConfig() throws Exception {
        // Khởi tạo cặp khóa RSA 2048-bit
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        this.keyPair = keyGenerator.generateKeyPair();
    }
    @Bean
    public String keyId()
    {
        return keyId;
    }
    @Bean
    public RSAPrivateKey privateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    @Bean
    public RSAPublicKey publicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

}
