package com.dangbun.domain.member.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.member.refactor.application.port.out.GetMemberByInviteCodePort;
import com.dangbun.domain.member.refactor.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.refactor.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.refactor.domain.Member;
import com.dangbun.domain.place.refactor.exception.custom.InvalidInviteCodeException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.dangbun.domain.place.refactor.exception.status.PlaceExceptionResponse.INVALID_INVITE_CODE;

@PersistenceAdapter
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberCommandPort, MemberQueryPort, GetMemberByInviteCodePort {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    // ===== MemberCommandPort =====

    @Override
    public void save(Member member) {
        memberRepository.save(memberMapper.mapToJpaEntity(member));
    }

    @Override
    public void delete(Member member) {
        memberRepository.deleteById(member.getMemberId());
    }

    // ===== MemberQueryPort =====

    @Override
    public List<Member> findByPlaceId(Long placeId) {
        return memberRepository.findByPlace_PlaceId(placeId).stream()
                .map(memberMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Member> findWaitingMembersByPlaceId(Long placeId) {
        return memberRepository.findByPlace_PlaceIdAndStatusIsFalseOrderByNameAsc(placeId).stream()
                .map(memberMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public Optional<Member> findByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .map(memberMapper::mapToDomainEntity);
    }

    @Override
    public Optional<Member> findByPlaceIdAndName(Long placeId, String name) {
        return memberRepository.findByPlace_PlaceIdAndName(placeId, name)
                .map(memberMapper::mapToDomainEntity);
    }

    // ===== GetMemberByInviteCodePort =====

    @Override
    public Member getMember(String inviteCode) {
        MemberJpaEntity member = memberRepository.findWithPlaceByInviteCode(inviteCode).stream().findAny()
                .orElseThrow(() -> new InvalidInviteCodeException(INVALID_INVITE_CODE));

        return memberMapper.mapToDomainEntity(member);
    }
}
