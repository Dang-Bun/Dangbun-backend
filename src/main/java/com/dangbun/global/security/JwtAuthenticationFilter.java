package com.dangbun.global.security;

import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String token = getAccessToken(request);

            log.info("jwt filter is running");

            if (token != null && !token.equalsIgnoreCase("null")) {
                authenticate(request, response, token);
            }
            if (token == null) {
                throw new RuntimeException("No Bearer Token in request");
            }
        } catch (RuntimeException e) {
            logger.error(e);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        try {

            String userId = tokenProvider.validateAndGetUserId(accessToken);
            log.info("Authenticated user ID : " + userId);
            AbstractAuthenticationToken authentication = createAuthenticationToken(request, userId);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
            Cookie expiredCookie = new Cookie("accessToken", null);
            expiredCookie.setPath("/");
            expiredCookie.setMaxAge(0);
            expiredCookie.setHttpOnly(true);
            response.addCookie(expiredCookie);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":\"TOKEN_EXPIRED\",\"message\":\"토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            refreshAuthentication(accessToken, request, response);

            return;
        }
    }

    private void refreshAuthentication(String accessToken, HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = getRefreshToken(request);
            if(isValidRefreshToken(refreshToken, accessToken)){

                String userId = tokenProvider.validateAndGetUserId(refreshToken);
                User user = userRepository.findById(Long.valueOf(userId))
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


                AbstractAuthenticationToken authentication = createAuthenticationToken(request, userId);

                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);

                String newAccessToken = tokenProvider.createAccessToken(user);
                response.setHeader("Authorization", "Bearer " + newAccessToken);
            }
        } catch (Exception e) {
            log.warn("refreshAuthentication 실패: {}", e.getMessage());

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isValidRefreshToken(String refreshToken, String accessToken) {
        try {
            Claims refreshClaims = tokenProvider.parseClaims(refreshToken);
            Claims accessClaims = tokenProvider.parseClaims(accessToken);

            String tokenType = refreshClaims.get("token_type", String.class);
            if (!"refresh".equals(tokenType)) {
                return false;
            }

            String refreshUserId = refreshClaims.getSubject();
            String accessUserId = accessClaims.getSubject();
            return refreshUserId.equals(accessUserId);
        }catch (ExpiredJwtException e){
            logger.error(e);
            return false;
        }catch (Exception e){
            logger.error(e);
            return false;
        }
    }

    private AbstractAuthenticationToken createAuthenticationToken(HttpServletRequest request, String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        CustomUserDetails userDetails = new CustomUserDetails(user);


        AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getRefreshToken(HttpServletRequest request) {
        try {
            if (request.getCookies() != null) {
                return Arrays.stream(request.getCookies())
                        .filter(cookie -> "refreshToken".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}
