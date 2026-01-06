package com.dangbun.domain.user;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LoginTypeRequestConverter implements Converter<String, LoginType> {

    @Override
    public LoginType convert(String source) {
        try {
            return LoginType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 로그인 타입입니다. " + source);
        }
    }
}
