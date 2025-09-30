package com.dangbun.global.security.jwt;


import com.dangbun.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

import static com.dangbun.global.security.jwt.TokenType.*;

@RequiredArgsConstructor
@Service
public class JwtProvider {

    @Value("${jwt.secret}")
    private String SECRET;
    private Key key;

    public final static String issuer = "dangbun app";


    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String createAccessToken(String email){

        return buildToken(email, issuer, ACCESS.getName());
    }

    public String createRefreshToken(User user){
        return buildToken(user.getEmail(), issuer, REFRESH.getName());
    }


    private String buildToken(String email, String issuer, String type){
        Date now = new Date();
        Date expiryDate;

        if (ACCESS.getName().equals(type)) {
            expiryDate = new Date(now.getTime() + TokenAge.ACCESS.getAge());
        } else if (REFRESH.getName().equals(type)) {
            expiryDate = new Date(now.getTime() + TokenAge.REFRESH.getAge());
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
