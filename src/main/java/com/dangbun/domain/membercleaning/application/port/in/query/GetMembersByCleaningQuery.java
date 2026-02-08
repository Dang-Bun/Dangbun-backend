package com.dangbun.domain.membercleaning.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

import java.util.List;

public interface GetMembersByCleaningQuery {

    List<Member> getMembersByCleaningId(Long cleaningId);
}
