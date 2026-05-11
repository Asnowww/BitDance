package com.bitdance.iam.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtService(
        @Value("${bitdance.jwt.secret}") String secret,
        @Value("${bitdance.jwt.access-ttl-minutes:60}") long accessMinutes
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("bitdance.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTtl = Duration.ofMinutes(accessMinutes);
    }

    public String issueAccessToken(long userId, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claims(Map.of("roles", roles))
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTtl.toMillis()))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
