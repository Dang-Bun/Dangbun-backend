package com.dangbun.domain.user.service;

import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public void createUser(){

    }


    public void login(){

    }

    public void logout(){

    }

    public void createPassword(){

    }

    public void deleteUser(){

    }




    public User findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
