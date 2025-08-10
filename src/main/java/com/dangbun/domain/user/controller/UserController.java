package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.*;
import com.dangbun.domain.user.dto.response.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.domain.user.service.UserService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Validated
@Tag(name = "User", description = "UserController - 회원 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @Operation(summary = "인증번호 생성(비밀번호 재설정용)",description = "이메일 인증번호를 생성합니다.(비밀번호 재설정 용)")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_EMAIL"}
    )
    @PostMapping("/email-code")
    public ResponseEntity<BaseResponse<?>> generatePasswordAuthCode(@RequestBody PostUserAuthCodeRequest request) {
        String email = request.email();
        userService.sendFindPasswordAuthCode(email);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "인증번호 생성(회원가입 용)",description = "이메일 인증번호를 생성합니다.(회원가입 용)")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL"}
    )
    @PostMapping("/signup/email-code")
    public ResponseEntity<BaseResponse<?>> generateSignupAuthCode(@RequestBody PostUserAuthCodeRequest request) {
        String email = request.email();
        userService.sendSignupAuthCode(email);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @Operation(summary = "회원가입")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL","INVALID_PASSWORD", "INVALID_CERT_CODE"}
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<?>> signUp(@RequestBody PostUserSignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "로그인")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER","INVALID_PASSWORD", "DELETE_MEMBER"}
    )
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<PostUserLoginResponse>> login(@RequestBody PostUserLoginRequest request) {
        return ResponseEntity.ok(BaseResponse.ok(userService.login(request)));
    }


    @Operation(summary = "로그아웃",description = "로그아웃 시 클라이언트 측에서 bearer token을 삭제해주어야 합니다")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<?>> logout(@RequestHeader("Authorization") String bearerToken) {
        userService.logout(bearerToken);
        return ResponseEntity.ok(BaseResponse.ok(null));

    }


    @Operation(summary = "비밀번호 재설정")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER", "INVALID_CERT_CODE"}
    )
    @PostMapping("/me/password")
    public ResponseEntity<BaseResponse<?>> updatePassword(@RequestBody @Valid PostUserPasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @Operation(summary = "당번 서비스 탈퇴")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = { "INVALID_EMAIL"}
    )
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<?>> deleteCurrentUser(@AuthenticationPrincipal(expression = "user") User user,
                                                             @RequestBody DeleteUserAccountRequest request) {
        userService.deleteCurrentUser(user, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "내 회원 정보 조회",description = "회원가입 시 입력한 이름, 이메일 정보를 조회합니다.")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<?>> getMyInfo(
            @AuthenticationPrincipal(expression = "user") User user
    ) {

        return ResponseEntity.ok(BaseResponse.ok(userService.getMyInfo(user)));
    }

}
