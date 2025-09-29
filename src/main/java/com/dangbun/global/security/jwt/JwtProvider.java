package com.dangbun.global.security.jwt;


import com.dangbun.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class JwtProvider {

    private final StringRedisTemplate redisTemplate;
    @Value("${jwt.secret}")
    private String SECRET;
    private Key key;

    public final static Long ACCESS_TOKEN_MS = 1000L * 60 * 60 * 24 * 15;
    public final static Long REFRESH_TOKEN_MS = 1000L * 60 * 60 * 24 * 15;
    public final static String issuer = "dangbun app";


    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String createAccessToken(User user){

        return buildToken(user.getEmail(), issuer, "access");
    }

    public String createRefreshToken(User user){
        return buildToken(user.getEmail(), issuer, "refresh");
    }


    private String buildToken(String email, String issuer, String type){
        Date now = new Date();
        Date expiryDate;

        if ("access".equals(type)) {
            expiryDate = new Date(now.getTime() + ACCESS_TOKEN_MS);
        } else if ("refresh".equals(type)) {
            expiryDate = new Date(now.getTime() + REFRESH_TOKEN_MS);
        } else {
            throw new IllegalArgumentException("Invalid token type: " + type);
        }

        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .claim("token_type", type)
                .compact();
    }


}
