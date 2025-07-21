package com.dangbun.global.security;


import com.dangbun.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final RedisTemplate redisTemplate;
    @Value("${jwt.secret}")
    private String SECRET;
    private Key key;

    public final static Long TOKEN_VALIDATION_MS = 1000L * 60;
    public final static Long REFRESH_VALIDATION_MS = 1000L * 120;


    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String createAccessToken(User user){

        return buildToken(user.getId(), "dangbun app", "access");
    }

    public String createRefreshToken(User user){
        return buildToken(user.getId(), "dangbun app", "refresh");
    }

    public String validateAndGetUserId(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return  claims.getSubject();
    }


    private String buildToken(Long userId, String issuer, String type){
        Date now = new Date();
        Date expiryDate;

        if ("access".equals(type)) {
            expiryDate = new Date(now.getTime() + TOKEN_VALIDATION_MS);
        } else if ("refresh".equals(type)) {
            expiryDate = new Date(now.getTime() + REFRESH_VALIDATION_MS);
        } else {
            throw new IllegalArgumentException("Invalid token type: " + type);
        }

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .claim("token_type", type)
                .compact();
    }

    public void saveRefreshToken(Long userId ,String token){
        redisTemplate.opsForValue()
                .set("refreshToken:"+userId, token, Duration.ofMillis(REFRESH_VALIDATION_MS));
    }

    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
