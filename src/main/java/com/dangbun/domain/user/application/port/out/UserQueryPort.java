package com.dangbun.domain.user.application.port.out;

import com.dangbun.domain.user.domain.User;

import java.util.Optional;

public interface UserQueryPort {

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    Optional<User> findBySocialId(String socialId);

    boolean existsByEmail(String email);
}
