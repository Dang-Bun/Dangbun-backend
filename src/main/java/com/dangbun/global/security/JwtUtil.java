package com.dangbun.global.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    public final static Long TOKEN_VALIDATION_MS = 1000L * 60;
    public final static Long REFRESH_VALIDATION_MS = 1000L * 120;

    @Value("${jwt.secret}")
    private String SECRET;
    private Key key;


    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String parseAccessToken(String bearerToken) {
        String accessToken = bearerToken.replace("Bearer ", "");
        return null;
    }

    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpiration(String token){
        return parseClaims(token).getExpiration();
    }

}
