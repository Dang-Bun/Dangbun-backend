package com.dangbun.domain.user.detail;

import com.dangbun.domain.user.adapter.out.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final SpringDataUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new CustomUserDetails(userRepository.findByEmail(email).orElseThrow(RuntimeException::new));
    }
}
