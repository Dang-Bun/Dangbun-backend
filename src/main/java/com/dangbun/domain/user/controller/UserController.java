package com.dangbun.domain.user.controller;

import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;


    @PostMapping("/email-code")
    public void generateAuthCode(){

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
