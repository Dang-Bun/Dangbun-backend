package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.AuthCodeRequest;
import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.ErrorCode;
import com.dangbun.domain.user.service.UserService;
import com.dangbun.global.response.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.dangbun.domain.user.exception.ErrorCode.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private ErrorCode errorCode;

    @PostMapping("/email-code")
    public ResponseDTO generateAuthCode(@RequestBody @Valid AuthCodeRequest request){
        String email = request.getEmail();
        try {
            userService.sendAuthCode(email);
        }catch (RuntimeException e){
            log.error(e.toString());
            return new ResponseDTO(EXIST_EMAIL.getCode(), EXIST_EMAIL.getMessage());
        }
        return null;
    }

    @PostMapping("/signup")
    public void signUp(@RequestBody @Validated PostUserSignUpRequest request){
        User user = new User();


    }

    @PostMapping("/login")
    public void login(){

    }

    @PostMapping("/logout")
    public void logout(){

    }

    @PatchMapping("/password/reset")
    public void updatePassword(){

    }


    @DeleteMapping("/me")
    public void deleteCurrentUser(){

    }




}
