package org.example.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.micrometer.core.instrument.config.InvalidConfigurationException;
import org.example.infrastructure.config.JwtProperties;
import org.example.infrastructure.config.TokenType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtTokenProvider
{
    private final JwtProperties props;
    private final RSAPrivateKey privateKey;

    public JwtTokenProvider(JwtProperties props, RSAPrivateKey privateKey) {
        this.props = props;
        this.privateKey = privateKey;
    }

    public String createToken(TokenType type, String subject, Map<String, Object> extraClaims) {
        long ttl = props.getTtlSeconds().getOrDefault(type, 3600L);
        Instant now = Instant.now();
        JwtBuilder b = Jwts.builder()
                .header().add("kid", "s-jwt-key")
                .and()
                .issuer(props.getIssuer())
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttl)))
                .claim("typ", type.name());
        if (extraClaims != null) b.claims().add(extraClaims);

        return b.signWith(privateKey, Jwts.SIG.RS256).compact();
    }

}
