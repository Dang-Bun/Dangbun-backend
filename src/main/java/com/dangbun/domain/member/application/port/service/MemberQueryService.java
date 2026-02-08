package com.dangbun.domain.member.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.application.port.in.query.GetMembersByUserIdQuery;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService implements GetMembersByUserIdQuery {

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
}
