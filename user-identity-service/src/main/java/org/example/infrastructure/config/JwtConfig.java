package org.example.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;


import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class JwtConfig {
    private final RSAPublicKey publicKey;
    private final JwtProperties jwtProperties;

    public JwtConfig(RSAPublicKey publicKey, JwtProperties jwtProperties) {
        this.publicKey = publicKey;
        this.jwtProperties = jwtProperties;
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        // Khởi tạo decoder với public key
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(this.publicKey).build();

        // 1. Validator kiểm tra thời hạn (Timestamp) - Cho phép độ trễ (clock skew) 60 giây
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(60));
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer());

        OAuth2TokenValidator<Jwt> delegatingValidator = new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator);

        // Gán validator cho decoder
        jwtDecoder.setJwtValidator(delegatingValidator);

        return jwtDecoder;
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Trích xuất Roles (Thêm tiền tố ROLE_ để dùng với .hasRole())
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            // Trích xuất Permissions (Giữ nguyên tên gốc để dùng với .hasAuthority())
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));
            }
            return authorities;
        });

        return converter;
    }
}
