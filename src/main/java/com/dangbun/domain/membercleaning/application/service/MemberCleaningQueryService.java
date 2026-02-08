package com.dangbun.domain.membercleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.in.query.GetCleaningInfoByMemberQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForCalendarQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForDutyQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMembersByCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCleaningQueryService implements GetMembersByCleaningQuery, GetCleaningInfoByMemberQuery, GetMemberCleaningForDutyQuery, GetMemberCleaningForCalendarQuery, GetMemberCleaningForCleaningQuery {

    private final MemberCleaningQueryPort memberCleaningQueryPort;

    @Override
    public List<Member> getMembersByCleaningId(Long cleaningId) {
        return memberCleaningQueryPort.findMembersByCleaningId(cleaningId);
    }

    @Override
    public List<Long> getCleaningIdsByMemberId(Long memberId) {
        return memberCleaningQueryPort.findCleaningIdsByMemberId(memberId);
    }

    @Override
    public Integer getCleaningCountByMemberId(Long memberId) {
        return memberCleaningQueryPort.countCleaningsByMemberId(memberId);
    }

    // GetMemberCleaningForDutyQuery 구현
    @Override
    public List<String> findMemberNamesByCleaningId(Long cleaningId) {
        return memberCleaningQueryPort.findMemberNamesByCleaningId(cleaningId);
    }

    @Override
    public Integer countMembersByCleaningId(Long cleaningId) {
        return memberCleaningQueryPort.countMembersByCleaningId(cleaningId);
    }

    // GetMemberCleaningForCalendarQuery 구현
    @Override
    public List<Long> findCleaningIdsByMemberId(Long memberId) {
        return memberCleaningQueryPort.findCleaningIdsByMemberId(memberId);
    }
}
