package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.*;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.domain.user.service.UserService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
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

    @PostMapping("/email-code")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL"}
    )
    public ResponseEntity<?> generateAuthCode(@RequestBody PostUserCertCodeRequest request) {
        String email = request.email();
        userService.sendAuthCode(email);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/signup")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL","INVALID_PASSWORD"}
    )
    public ResponseEntity<?> signUp(@RequestBody PostUserSignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/login")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_PASSWORD"}
    )
    public ResponseEntity<?> login(@RequestBody PostUserLoginRequest request) {
        BaseResponse<?> response = userService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        userService.logout(bearerToken);
        return ResponseEntity.ok(BaseResponse.ok(null));

    }


    @PostMapping("/me/password")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER"}
    )
    public ResponseEntity<BaseResponse<?>> updatePassword(@RequestBody @Valid PostUserPasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @DeleteMapping("/me")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_PASSWORD"}
    )
    public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails customUser,
                                                             @RequestBody DeleteUserAccountRequest request) {
        userService.deleteCurrentUser(customUser, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

}
