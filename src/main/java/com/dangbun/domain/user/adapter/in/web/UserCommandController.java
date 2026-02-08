package com.dangbun.domain.user.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.user.adapter.in.web.dto.request.DeleteUserAccountRequest;
import com.dangbun.domain.user.adapter.in.web.dto.request.PostUserAuthCodeRequest;
import com.dangbun.domain.user.adapter.in.web.dto.request.PostUserPasswordUpdateRequest;
import com.dangbun.domain.user.adapter.in.web.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.application.port.in.command.DeleteUserCommand;
import com.dangbun.domain.user.application.port.in.command.SignupCommand;
import com.dangbun.domain.user.application.port.in.command.UpdatePasswordCommand;
import com.dangbun.domain.user.application.port.in.command.UserCommandUseCase;
import com.dangbun.domain.user.application.port.in.command.*;
import com.dangbun.domain.user.detail.CustomUserDetails;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "User", description = "UserController - 회원 관련 API")
@RequiredArgsConstructor
@WebAdapter(path = "/users")
public class UserCommandController {

    private final UserCommandUseCase userCommandUseCase;

    @Operation(summary = "인증번호 생성(회원가입 용)", description = "이메일 인증번호를 생성합니다.(회원가입 용)")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL", "AUTH_CODE_SENT"}
    )
    @PostMapping("/signup/email-code")
    public ResponseEntity<BaseResponse<?>> generateSignupAuthCode(@RequestBody PostUserAuthCodeRequest request) {
        userCommandUseCase.sendSignupAuthCode(request.email());
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "회원가입")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL", "INVALID_PASSWORD", "INVALID_CERT_CODE"}
    )
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<?>> signUp(@RequestBody @Valid PostUserSignUpRequest request) {
        SignupCommand command = new SignupCommand(
                request.name(),
                request.email(),
                request.password(),
                request.certCode()
        );
        userCommandUseCase.signup(command);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "비밀번호 재설정")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER", "INVALID_CERT_CODE"}
    )
    @PostMapping("/me/password")
    public ResponseEntity<BaseResponse<?>> updatePassword(@RequestBody @Valid PostUserPasswordUpdateRequest request) {
        UpdatePasswordCommand command = new UpdatePasswordCommand(
                request.email(),
                request.certCode(),
                request.password()
        );
        userCommandUseCase.updatePassword(command);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번 서비스 탈퇴")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_EMAIL"}
    )
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<?>> deleteCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DeleteUserAccountRequest request
    ) {
        DeleteUserCommand command = new DeleteUserCommand(
                userDetails.getUser().getUserId(),
                request.email()
        );
        userCommandUseCase.deleteUser(command);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
