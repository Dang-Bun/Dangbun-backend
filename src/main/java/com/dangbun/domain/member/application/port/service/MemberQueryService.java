package com.dangbun.domain.member.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.application.port.in.query.GetMemberForCalendarQuery;
import com.dangbun.domain.member.application.port.in.query.GetMemberForCleaningQuery;
import com.dangbun.domain.member.application.port.in.query.GetMembersByUserIdQuery;
import com.dangbun.domain.member.application.port.in.query.GetMembersForDutyQuery;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService implements GetMembersByUserIdQuery, GetMembersForDutyQuery, GetMemberForCalendarQuery, GetMemberForCleaningQuery {

    private final MemberQueryPort memberQueryPort;

    @Override
    public List<Member> getMembersByUserId(Long userId) {
        return memberQueryPort.findByUserId(userId);
    }

    @Override
    public Optional<Member> getMemberByUserIdAndPlaceId(Long userId, Long placeId) {
        return memberQueryPort.findByUserIdAndPlaceId(userId, placeId);
    }

    @Override
    public Optional<Member> getFirstMemberByPlaceId(Long placeId) {
        return memberQueryPort.findFirstByPlaceId(placeId);
    }

    @Override
    public List<GetMembersForDutyQuery.MemberInfo> findAllByIds(List<Long> memberIds) {
        return memberQueryPort.findAllByIds(memberIds).stream()
                .map(m -> new GetMembersForDutyQuery.MemberInfo(m.getMemberId(), m.getName()))
                .toList();
    }

    // GetMemberForCalendarQuery 구현
    @Override
    public Optional<String> findMemberNameById(Long memberId) {
        return memberQueryPort.findById(memberId) != null
                ? Optional.of(memberQueryPort.findById(memberId).getName())
                : Optional.empty();
    }

    // GetMemberForCleaningQuery 구현
    @Override
    public List<GetMemberForCleaningQuery.MemberInfo> findAllByNameIn(List<String> names) {
        return memberQueryPort.findAllByNameIn(names).stream()
                .map(m -> new GetMemberForCleaningQuery.MemberInfo(m.getMemberId(), m.getName()))
                .toList();
    }
}
