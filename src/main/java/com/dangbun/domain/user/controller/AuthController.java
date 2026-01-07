package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.request.auth.PostKakaoLoginRequest;
import com.dangbun.domain.user.entity.LoginType;
import com.dangbun.domain.user.dto.request.auth.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.domain.user.service.auth.LoginService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import com.dangbun.global.security.jwt.TokenAge;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.dangbun.global.security.jwt.TokenType.REFRESH;

/**
 * 로그인, 토큰 재발급
 */

@RequiredArgsConstructor
@RestController
@Slf4j
@Validated
@RequestMapping("/users")
public class AuthController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> loginDefault(@RequestBody PostUserLoginRequest request) {
        LoginResult result = handleLogin(LoginType.EMAIL, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.response().accessToken())
                .body(BaseResponse.ok(result.response()));
    }

    @GetMapping("/login/kakao")
    public ResponseEntity<Void> loginKakao(@RequestParam(defaultValue = "") String code) {
        LoginResult result = handleLogin(LoginType.KAKAO, PostKakaoLoginRequest.of(code, "", "", ""));

        return ResponseEntity.status(org.springframework.http.HttpStatus.SEE_OTHER)
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.response().accessToken())
                .location(java.net.URI.create("https://dangbun-frontend-virid.vercel.app/myPlace"))
                .build();
    }

    private LoginResult handleLogin(LoginType type, LoginRequest request) {
        PostUserLoginResponse response = (PostUserLoginResponse) loginService.login(type, request);

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH.getName(), response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(TokenAge.REFRESH.getAge() / 1000)
                .build();

        return new LoginResult(response, refreshCookie);
    }

    private record LoginResult(PostUserLoginResponse response, ResponseCookie cookie) {}
}