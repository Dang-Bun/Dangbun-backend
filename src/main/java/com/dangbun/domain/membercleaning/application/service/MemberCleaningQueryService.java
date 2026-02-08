package com.dangbun.domain.membercleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMembersByCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCleaningQueryService implements GetMembersByCleaningQuery {

    private final MemberCleaningQueryPort memberCleaningQueryPort;

    @Override
    public List<Member> getMembersByCleaningId(Long cleaningId) {
        return memberCleaningQueryPort.findMembersByCleaningId(cleaningId);
    }
}
