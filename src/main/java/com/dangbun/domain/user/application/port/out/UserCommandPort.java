package com.dangbun.domain.user.application.port.out;

import com.dangbun.domain.user.domain.User;

public interface UserCommandPort {

    User save(User user);

    void delete(User user);

    void deleteById(Long userId);
}
