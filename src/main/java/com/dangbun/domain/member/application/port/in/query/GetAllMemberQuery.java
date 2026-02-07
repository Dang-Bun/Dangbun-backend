package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

import java.util.List;

public interface GetAllMemberQuery {
    List<Member> findAllByIds(List<Long> memberIds);
}
