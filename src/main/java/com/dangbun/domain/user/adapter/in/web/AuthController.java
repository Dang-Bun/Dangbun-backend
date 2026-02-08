package com.dangbun.domain.user.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.user.adapter.in.web.dto.request.auth.PostKakaoLoginRequest;
import com.dangbun.domain.user.adapter.in.web.dto.request.auth.PostUserLoginRequest;
import com.dangbun.domain.user.adapter.in.web.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.application.port.in.command.AuthUseCase;
import com.dangbun.domain.user.application.port.in.command.EmailLoginCommand;
import com.dangbun.domain.user.application.port.in.command.KakaoLoginCommand;
import com.dangbun.domain.user.application.port.in.command.*;
import com.dangbun.domain.user.application.port.in.query.AuthTokenResult;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import com.dangbun.global.security.jwt.TokenAge;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.dangbun.global.security.jwt.TokenType.REFRESH;

@Validated
@Tag(name = "User", description = "AuthController - 로그인 및 토큰 발급 관련 API")
@RequiredArgsConstructor
@WebAdapter(path = "/users")
public class AuthController {

    private final AuthUseCase authUseCase;

    @Operation(summary = "일반 이메일 로그인", description = "이메일과 비밀번호로 요청하면 토큰을 발급함")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER", "INVALID_PASSWORD", "DELETE_MEMBER"}
    )
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> loginDefault(
            @RequestBody @Valid PostUserLoginRequest request
    ) {
        EmailLoginCommand command = new EmailLoginCommand(
                request.email(),
                request.password()
        );
        AuthTokenResult result = authUseCase.loginWithEmail(command);
        return buildLoginResponse(result);
    }

    @Operation(summary = "카카오 로그인", description = "인가코드로 요청하면 토큰을 발급함")
    @PostMapping("/login/kakao")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> loginKakao(
            @RequestBody PostKakaoLoginRequest request
    ) {
        KakaoLoginCommand command = new KakaoLoginCommand(
                request.code(),
                request.error(),
                request.error_description(),
                request.state()
        );
        AuthTokenResult result = authUseCase.loginWithKakao(command);
        return buildLoginResponse(result);
    }

    private ResponseEntity<BaseResponse<PostUserLoginResponse>> buildLoginResponse(AuthTokenResult result) {
        PostUserLoginResponse response = new PostUserLoginResponse(
                result.accessToken(),
                result.refreshToken()
        );

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH.getName(), response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(TokenAge.REFRESH.getAge() / 1000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.accessToken())
                .body(BaseResponse.ok(response));
    }
}
