package com.dangbun.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handleJwtException(request, response, ex);
        }
    }

    private void handleJwtException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception ex
    ) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot write jwt error. uri = {}, ex = {}", request.getRequestURI(), ex.toString());
            return;
        }

        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String code = "JWT_ERROR";
        String message = ex.getMessage();

        response.resetBuffer();
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "success", false,
                "code", code,
                "message", message,
                "path", request.getRequestURI()
        );

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();
        // close will be called by container

    }
}
