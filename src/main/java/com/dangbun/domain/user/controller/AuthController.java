package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.request.auth.PostKakaoLoginRequest;
import com.dangbun.domain.user.LoginType;
import com.dangbun.domain.user.dto.request.auth.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.domain.user.service.auth.LoginService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import com.dangbun.global.security.jwt.TokenAge;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "User(Auth)", description = "AuthController - 로그인, 토큰재발급 관련 API")
public class AuthController {

    private final LoginService loginService;


    @Operation(summary = "일반 이메일 로그인")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER", "INVALID_PASSWORD", "DELETE_MEMBER"}
    )
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> loginDefault(@RequestBody PostUserLoginRequest request) {
        return processLogin(LoginType.EMAIL, request);
    }

    @Operation(summary = "카카오 로그인")
    @DocumentedApiErrors(
            value = {},
            includes = {}
    )
    @GetMapping("/login/kakao")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> loginKakao(@RequestParam(defaultValue = "") String code,
                                                                          @RequestParam(defaultValue = "") String error,
                                                                          @RequestParam(defaultValue = "") String error_description,
                                                                          @RequestParam(defaultValue = "") String state)
    {

        return processLogin(LoginType.KAKAO, PostKakaoLoginRequest.of(code, error, error_description, state));
    }



    private ResponseEntity<BaseResponse<PostUserLoginResponse>> processLogin(@PathVariable LoginType type, @RequestBody LoginRequest request) {
        PostUserLoginResponse response = (PostUserLoginResponse) loginService.login(type, request);

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH.getName(), response.refreshToken())
                .httpOnly(true).secure(true).sameSite("None").path("/")
                .maxAge(TokenAge.REFRESH.getAge() / 1000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.accessToken())
                .body(BaseResponse.ok(response));
    }


}
