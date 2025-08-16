package com.dangbun.global.security;

import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.exception.InvalidRefreshJWTException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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

import static com.dangbun.global.response.status.BaseExceptionResponse.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            if(path.contains("/users/login")){
                filterChain.doFilter(request, response);
                return;
            }
            String token = jwtUtil.parseAccessToken(request.getHeader("Authorization"));

            log.info("jwt filter is running");

            if (token != null && !token.equalsIgnoreCase("null")) {
                boolean authenticated = authenticate(request, response, token);
                if (!authenticated) {
                    filterChain.doFilter(request, response);
                }
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            createErrorResponse(response);
            logger.error(e);
        }
    }

    private boolean authenticate(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        try {

            String userId = jwtUtil.validateAndGetUserId(accessToken);
            log.info("Authenticated user ID : " + userId);
            AbstractAuthenticationToken authentication = createAuthenticationToken(request, userId);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            return true;
        } catch (ExpiredJwtException ex) {

            log.warn("Expired JWT token: {}", ex.getMessage());
            Cookie expiredCookie = new Cookie("accessToken", null);
            expiredCookie.setPath("/");
            expiredCookie.setMaxAge(0);
            expiredCookie.setHttpOnly(true);
            response.addCookie(expiredCookie);

            return refreshAuthentication(request, response);
        }
    }

    private boolean refreshAuthentication(HttpServletRequest request, HttpServletResponse response)  {
        try {
            String refreshToken = getRefreshToken(request);
            Claims claims = jwtUtil.parseClaims(refreshToken);
            if (!isValidRefreshToken(refreshToken,claims)) {
                throw new RuntimeException("Invalid refresh Token");
            }
            String userId = claims.getSubject();
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


            AbstractAuthenticationToken authentication = createAuthenticationToken(request, userId);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            String newAccessToken = tokenProvider.createAccessToken(user);
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            return true;

        } catch (Exception e) {
            log.warn("refreshAuthentication 실패: {}", e.getMessage());

            SecurityContextHolder.clearContext();
            createErrorResponse(response);
            return false;
        }
    }

    private boolean isValidRefreshToken(String refreshToken,Claims refreshClaims) {
        try {
            String tokenType = refreshClaims.get("token_type", String.class);
            if(!"refresh".equals(tokenType)) return false;

            String userId = refreshClaims.getSubject();
            String saved = (String)redisTemplate.opsForValue().get("refreshToken:"+userId);
            return saved != null && saved.equals(refreshToken);

        } catch (Exception e){
            log.error("RefreshToken 유효성 검사 중 오류",e);
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

    private static void createErrorResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":"+INVALID_JWT.getCode()+",\"message\":\"토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
        } catch (IOException ex) {
            log.error("Error writing response", ex);
        }
    }
}
