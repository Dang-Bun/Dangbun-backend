package com.dangbun.global.security;

import com.dangbun.global.exception.InvalidRefreshJWTException;
import com.dangbun.global.response.BaseErrorResponse;
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
            logger.error(e);
            setErrorResponse(AUTH_UNAUTHENTICATED, response, HttpStatus.UNAUTHORIZED);
        }catch (InvalidRefreshJWTException e){
            // 유효하지 않은 Refresh JWT
            logger.error(e);
            setErrorResponse(INVALID_REFRESH_TOKEN, response, HttpStatus.UNAUTHORIZED);
        }
        catch(JwtException | IllegalStateException e){
            // 유효하지 않은 토큰
            logger.error(e);
            setErrorResponse(INVALID_JWT, response,HttpStatus.UNAUTHORIZED);
        }
    }

    private void setErrorResponse(BaseExceptionResponse exceptionResponse,
                                  HttpServletResponse response,
                                  HttpStatus status) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");

        BaseErrorResponse errorResponse = new BaseErrorResponse(exceptionResponse);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }


}
