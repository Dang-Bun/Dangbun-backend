package com.dangbun.global.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    private Key key;
    private JwtParser parser;


    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());

        this.parser = Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build();
    }

    private byte[] tryBase64Decode(String s) {
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String parseAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header is empty");
        }
        final String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }
        String token = authorizationHeader.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Bearer token is empty");
        }
        return token;
    }

    public Claims parseClaims(String token) {
        return parser
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public String validateAndGetUserId(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw e;
        }
    }


}
