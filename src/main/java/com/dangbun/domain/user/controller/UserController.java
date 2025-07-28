package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.*;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.domain.user.service.UserService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
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
    public ResponseEntity<?> generateAuthCode(@RequestBody @Valid PostUserCertCodeRequest request) {
        String email = request.email();
        userService.sendAuthCode(email);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/signup")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"EXIST_EMAIL","INVALID_PASSWORD"}
    )
    public ResponseEntity<?> signUp(@RequestBody @Valid PostUserSignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/login")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"INVALID_PASSWORD"}
    )
    public ResponseEntity<?> login(@RequestBody @Valid PostUserLoginRequest request) {
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
