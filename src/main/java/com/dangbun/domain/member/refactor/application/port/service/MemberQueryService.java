package com.dangbun.domain.member.refactor.application.port.service;

import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.exception.custom.MemberNotFoundException;
import com.dangbun.domain.member.refactor.application.port.in.query.*;
import com.dangbun.domain.member.refactor.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.refactor.domain.Member;
import com.dangbun.domain.member.refactor.domain.MemberRole;
import com.dangbun.domain.memberduty.refactor.adapter.out.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.member.original.response.status.MemberExceptionResponse.MEMBER_NOT_FOUND;

/*
 * TODO: 다른 도메인 헥사고날 아키텍처 전환 시 수정
 * 각 도메인의 Query Port를 통해 조회하도록 변경 필요
 * - MemberDutyRepository -> MemberDutyQueryPort
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberQueryService implements MemberQuery {

    private final MemberQueryPort memberQueryPort;
    private final MemberDutyRepository memberDutyRepository;

    @Override
    public MembersResult getMembers() {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> members = memberQueryPort.findByPlaceId(placeId);

        members.sort(Comparator
                .comparing((Member m) -> m.getRole() != MemberRole.MANAGER)
                .thenComparing(Member::getName, Comparator.nullsLast(String::compareTo)));

        Map<Member, List<String>> memberMap = new LinkedHashMap<>();
        Integer waitingMemberNumber = 0;

        for (Member member : members) {
            if (member.getStatus()) {
                /*
                 * TODO: MemberDuty 도메인 헥사고날 아키텍처 전환 시 수정
                 * MemberDutyRepository -> MemberDutyQueryPort
                 */
                List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllByMember_MemberId(member.getMemberId());
                List<String> dutyNames = new ArrayList<>();
                for (MemberDutyJpaEntity memberDutyJpaEntity : memberDuties) {
                    dutyNames.add(memberDutyJpaEntity.getDuty().getName());
                }
                memberMap.put(member, dutyNames);
            }
            if (!member.getStatus()) {
                waitingMemberNumber++;
            }
        }

        if (me.getRole().equals(com.dangbun.domain.member.original.entity.MemberRole.MEMBER)) {
            waitingMemberNumber = null;
        }

        List<MembersResult.MemberDto> memberDtos = memberMap.entrySet().stream()
                .map(entry -> new MembersResult.MemberDto(
                        entry.getKey().getMemberId(),
                        entry.getKey().getRole().getDisplayName(),
                        entry.getKey().getName(),
                        entry.getValue()
                )).toList();

        return new MembersResult(waitingMemberNumber, memberDtos);
    }

    @Override
    public MemberDetailResult getMember(Long memberId) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();
        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        /*
         * TODO: MemberDuty 도메인 헥사고날 아키텍처 전환 시 수정
         * MemberDutyRepository -> MemberDutyQueryPort
         */
        List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllByMember_MemberId(member.getMemberId());
        List<MemberDetailResult.DutyDto> dutyDtos = memberDuties.stream()
                .map(md -> new MemberDetailResult.DutyDto(
                        md.getDuty().getDutyId(),
                        md.getDuty().getName()
                )).toList();

        MemberDetailResult.MemberDto memberDto = new MemberDetailResult.MemberDto(
                member.getName(),
                member.getRole().getDisplayName(),
                member.getInformation()
        );

        return new MemberDetailResult(memberDto, dutyDtos);
    }

    @Override
    public WaitingMembersResult getWaitingMembers() {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> members = memberQueryPort.findWaitingMembersByPlaceId(placeId);

        List<WaitingMembersResult.MemberDto> memberDtos = members.stream()
                .map(m -> new WaitingMembersResult.MemberDto(
                        m.getMemberId(),
                        m.getName(),
                        m.getInformation(),
                        m.getCreatedAt().toLocalDate()
                )).toList();

        return new WaitingMembersResult(memberDtos);
    }

    @Override
    public MyInformationResult getMyInformation() {
        MemberJpaEntity me = MemberContext.get();
        return new MyInformationResult(
                me.getMemberId(),
                me.getName(),
                me.getRole().getDisplayName()
        );
    }

    @Override
    public MemberSearchResult searchByNameInPlace(Long placeId, String name) {
        return memberQueryPort.findByPlaceIdAndName(placeId, name)
                .map(m -> new MemberSearchResult(m.getMemberId(), m.getName()))
                .orElse(new MemberSearchResult(null, null));
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberQueryPort.findByMemberIdAndPlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
    }
}
