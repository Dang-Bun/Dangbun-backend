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

//    private boolean authenticate(HttpServletRequest request, HttpServletResponse response, String accessToken) {
//        try {
//
//            String userId = JwtUtil.getUserId(accessToken);
//            log.info("Authenticated user ID : " + userId);
//            AbstractAuthenticationToken authentication = createAuthenticationToken(request, userId);
//
//            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//            securityContext.setAuthentication(authentication);
//            SecurityContextHolder.setContext(securityContext);
//            return true;
//        } catch (ExpiredJwtException ex) {
//
//            log.warn("Expired JWT token: {}", ex.getMessage());
//            Cookie expiredCookie = new Cookie("accessToken", null);
//            expiredCookie.setPath("/");
//            expiredCookie.setMaxAge(0);
//            expiredCookie.setHttpOnly(true);
//            response.addCookie(expiredCookie);
//
//            return refreshAuthentication(request, response);
//        }
//    }

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
//            createErrorResponse(response);
            return false;
        }
    }

//    private boolean isValidRefreshToken(String refreshToken, Claims refreshClaims) {
//        try {
//            String tokenType = refreshClaims.get("token_type", String.class);
//            if (!"refresh".equals(tokenType)) return false;
//
//            String userId = refreshClaims.getSubject();
//            String saved = (String) redisTemplate.opsForValue().get("refreshToken:" + userId);
//            return saved != null && saved.equals(refreshToken);
//
//        } catch (Exception e) {
//            log.error("RefreshToken 유효성 검사 중 오류", e);
//            return false;
//        }
//    }



//    private static void createErrorResponse(HttpServletResponse response) {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json;charset=UTF-8");
//        try {
//            response.getWriter().write("{\"code\":"+INVALID_JWT.getCode()+",\"message\":\"토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
//        } catch (IOException ex) {
//            log.error("Error writing response", ex);
//        }
//    }
}
