package com.dangbun.domain.member.application.port.out;

import com.dangbun.domain.member.domain.Member;

public interface MemberCommandPort {
    void save(Member manager);
    void delete(Member member);
}
