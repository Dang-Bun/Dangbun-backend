package com.dangbun.domain.user.application.port.out;

/**
 * 토큰 블랙리스트 관리를 위한 아웃고잉 포트
 */
public interface TokenBlacklistPort {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}
