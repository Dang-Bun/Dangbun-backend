package com.dangbun.domain.member;

import com.dangbun.domain.member.entity.MemberJpaEntity;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.place.original.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.refactor.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import static com.dangbun.domain.place.original.response.status.PlaceExceptionResponse.INVALID_INVITE_CODE;

@PersistenceAdapter
@RequiredArgsConstructor
public class MemberCommandPersistenceAdapter implements MemberCommandPort, GetMemberByInviteCodePort {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    public void save(Member manager) {
        memberRepository.save(memberMapper.mapToJpaEntity(manager));
    }

    @Override
    public void delete(Member member) {
        memberRepository.delete(memberMapper.mapToJpaEntity(member));
    }

    @Override
    public Member getMember(String inviteCode) {
        MemberJpaEntity member = memberRepository.findWithPlaceByInviteCode(inviteCode).stream().findAny()
                .orElseThrow(() -> new InvalidInviteCodeException(INVALID_INVITE_CODE));

        return memberMapper.mapToDomainEntity(member);
    }
}
