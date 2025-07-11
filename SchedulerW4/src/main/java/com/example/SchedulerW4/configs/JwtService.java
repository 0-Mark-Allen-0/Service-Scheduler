package com.example.SchedulerW4.configs;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Service
public class JwtService {

    // Fallback order: app.jwt.secret → JWT_SECRET → default-dev-secret
    @Value("${app.jwt.secret:${JWT_SECRET:default-dev-secret}}")
    private String jwtSecret;

    // Fallback order: app.jwt.expiration-ms → JWT_EXPIRATION → 86400000 (1 day)
    @Value("${app.jwt.expiration-ms:${JWT_EXPIRATION:86400000}}")
    private long jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", Collections.singletonList("ROLE_" + role)) // ✅ Important!
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }


    public String extractEmail(String token) {
        return Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }



    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
