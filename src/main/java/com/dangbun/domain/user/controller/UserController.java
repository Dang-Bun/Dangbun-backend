package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.*;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.service.UserService;
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
    public ResponseEntity<?> generateAuthCode(@RequestBody @Valid PostUserCertCodeRequest request) {
        String email = request.email();
        userService.sendAuthCode(email);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid PostUserSignUpRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid PostUserLoginRequest request) {
        userService.login(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @PostMapping("/logout")
    public BaseResponse<?> logout() {

        return null;

    }


    @PostMapping("/me/password")
    public ResponseEntity<BaseResponse<?>> updatePassword(@RequestBody @Valid PostUserPasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails customUser,
                                                             @RequestBody DeleteUserAccountRequest request) {
        userService.deleteCurrentUser(customUser, request);
        return null;
    }


}
