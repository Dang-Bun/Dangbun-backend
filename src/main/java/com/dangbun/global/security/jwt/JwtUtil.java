package com.dangbun.global.security.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

import static com.dangbun.global.security.jwt.TokenPrefix.REFRESH;

@Slf4j
@Component
public class JwtUtil {

    private static String SECRET;
    private static Key KEY;

    private JwtUtil() {}

    @Value("${jwt.secret}")
    private void setSECRETAndKEY(String secret){
        SECRET = secret;
        KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String parseAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header is empty");
        }
        final String prefix = TokenPrefix.BEARER.getName();
        if (!authorizationHeader.startsWith(prefix)) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }
        String token = authorizationHeader.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Bearer token is empty");
        }
        return token;
    }

    public static String getRefreshToken(HttpServletRequest request) {
        try {
            if (request.getCookies() != null) {
                return Arrays.stream(request.getCookies())
                        .filter(cookie -> REFRESH.getName().equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            log.error("refreshToken error", e);
        }
        return null;
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(stripBearer(token))
                .getBody();
    }

    public static boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e){
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException e){
            return false;
        } catch (io.jsonwebtoken.security.SecurityException e){
            return false;
        } catch (IllegalArgumentException e){
            return false;
        }

    }

    public static Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }


    public static String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public static String stripBearer(String token) {
        if (token != null && token.startsWith(TokenPrefix.BEARER.getName())) {
            return token.substring(7);
        }
        return token;
    }

}
