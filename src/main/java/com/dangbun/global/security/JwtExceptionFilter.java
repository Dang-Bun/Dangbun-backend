package com.dangbun.global.security;

import com.dangbun.global.exception.InvalidRefreshJWTException;
import com.dangbun.global.response.status.BaseExceptionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            // 토큰 유효기간 만료

        }catch (InvalidRefreshJWTException e){
            // 유효하지 않은 Refresh JWT
            logger.error(e);
            setErrorResponse(INVALID_REFRESH_TOKEN, response, e);
        }
        catch(JwtException | IllegalStateException e){
            // 유효하지 않은 토큰
            logger.error(e);
            setErrorResponse(INVALID_JWT, response,e);
        }
    }

    private void setErrorResponse(BaseExceptionResponse baseExceptionResponse, HttpServletResponse response, RuntimeException e) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json; charset=UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(baseExceptionResponse));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable e) {
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");
    }



}
