package com.dangbun.domain.member;

public interface MemberCommandPort {
    void save(Member manager);
    void delete(Member member);
}
