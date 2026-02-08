package com.dangbun.domain.member.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.member.application.port.out.GetMemberByInviteCodePort;
import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.dangbun.domain.place.exception.status.PlaceExceptionResponse.INVALID_INVITE_CODE;

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

    @Override
    public List<Member> findAllByNameIn(List<String> members) {
        List<MemberJpaEntity> memberJpaEntities = memberRepository.findAllByNameIn(members);

        return memberJpaEntities.stream().map(memberMapper::mapToDomainEntity).toList();
    }

    @Override
    public List<Member> findAllByIds(List<Long> memberIds) {
        List<MemberJpaEntity> memberJpaEntities = memberRepository.findAllById(memberIds);

        return memberJpaEntities.stream().map(memberMapper::mapToDomainEntity).toList();
    }

    @Override
    public Page<Member> findByPlaceIdWithPageable(Long placeId, Pageable pageable) {
        Page<MemberJpaEntity> memberJpaEntityPage = memberRepository.findByPlace_PlaceId(placeId, pageable);

        return memberJpaEntityPage.map(memberMapper::mapToDomainEntity);
    }

    @Override
    public Page<Member> findByPlaceIdAndNameContaining(Long placeId, String searchName, Pageable pageable) {
        Page<MemberJpaEntity> memberJpaEntityPage = memberRepository.findByPlace_PlaceIdAndNameContaining(placeId, searchName, pageable);

        return memberJpaEntityPage.map(memberMapper::mapToDomainEntity);
    }

    @Override
    public Member findById(Long memberId) {
        MemberJpaEntity memberJpaEntity = memberRepository.findById(memberId).get();

        return memberMapper.mapToDomainEntity(memberJpaEntity);
    }

    @Override
    public List<Member> findByUserId(Long userId) {
        return memberRepository.findWithPlaceByUserId(userId).stream()
                .map(memberMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public Optional<Member> findByUserIdAndPlaceId(Long userId, Long placeId) {
        return memberRepository.findWithPlaceByUserIdAndPlaceId(userId, placeId)
                .map(memberMapper::mapToDomainEntity);
    }

    @Override
    public Optional<Member> findFirstByPlaceId(Long placeId) {
        return memberRepository.findByPlace_PlaceId(placeId).stream()
                .findFirst()
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
