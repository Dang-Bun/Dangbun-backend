package com.dangbun.global.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request, response);
        }catch(JwtException e){
            logger.error(e);
            setErrorResponse(HttpStatus.UNAUTHORIZED, response,e);
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable e) {
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");
    }


}
