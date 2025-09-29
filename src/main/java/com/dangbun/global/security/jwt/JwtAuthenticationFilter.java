package com.dangbun.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.dangbun.global.security.jwt.TokenPrefix.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final PathMatcher pathMatcher;
    private final UserDetailsService userDetailsService;


    private static final List<String> SKIP_URLS = List.of(
            "/users/login",
            "/users/signup",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/docs/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_URLS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        String token = JwtUtil.parseAccessToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        try {
            if (token != null && JwtUtil.validateToken(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(JwtUtil.getSubject(token));
                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            //Todo RefreshToken 로직 처리
            refreshAuthentication(request, response);
        } catch (Exception ex) {
            request.setAttribute("jwtException", ex);
            throw ex;
        } finally {

        }
    }

    private boolean refreshAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = JwtUtil.getRefreshToken(request);
            if(refreshToken!= null && JwtUtil.validateToken(refreshToken)){
                Claims claims = JwtUtil.parseToken(refreshToken);
                String email = claims.getSubject();
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                String newAccessToken = jwtProvider.createAccessToken(email);


                AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);

                response.setHeader(HttpHeaders.AUTHORIZATION, BEARER.getName() + newAccessToken);
            }

            return true;

        } catch (Exception e) {
            log.error("refreshAuthentication 실패: {}", e.getMessage());

            SecurityContextHolder.clearContext();
            return false;
        }
    }
}
