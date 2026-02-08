package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

public interface GetMemberQuery {
    Member getMemberById(Long memberId);
}
