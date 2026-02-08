package com.dangbun.domain.member.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.*;
import com.dangbun.domain.member.exception.custom.MemberNotFoundException;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForMemberQuery;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForMemberQuery.DutyInfo;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.member.exception.status.MemberExceptionResponse.MEMBER_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPageQueryService implements MemberQuery, GetAllMemberQuery, GetMemberPageQuery, GetMemberQuery {

    private final MemberQueryPort memberQueryPort;
    private final GetMemberDutyForMemberQuery getMemberDutyForMemberQuery;

    @Override
    public MembersResult getMembers() {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> members = memberQueryPort.findByPlaceId(placeId).stream()
                .sorted(Comparator
                        .comparing((Member m) -> m.getRole() != com.dangbun.domain.member.domain.MemberRole.MANAGER)
                        .thenComparing(Member::getName, Comparator.nullsLast(String::compareTo)))
                .toList();

        Map<Member, List<String>> memberMap = new LinkedHashMap<>();
        Integer waitingMemberNumber = 0;

        for (Member member : members) {
            if (member.getStatus()) {
                List<DutyInfo> dutyInfos = getMemberDutyForMemberQuery.findDutyInfosByMemberId(member.getMemberId());
                List<String> dutyNames = dutyInfos.stream()
                        .map(DutyInfo::dutyName)
                        .toList();
                memberMap.put(member, dutyNames);
            }
            if (!member.getStatus()) {
                waitingMemberNumber++;
            }
        }

        if (me.getRole().equals(MemberRole.MEMBER)) {
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

        List<DutyInfo> dutyInfos = getMemberDutyForMemberQuery.findDutyInfosByMemberId(member.getMemberId());
        List<MemberDetailResult.DutyDto> dutyDtos = dutyInfos.stream()
                .map(info -> new MemberDetailResult.DutyDto(
                        info.dutyId(),
                        info.dutyName()
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

    @Override
    public List<Member> findAllByIds(List<Long> memberIds) {
        return memberQueryPort.findAllByIds(memberIds);
    }

    @Override
    public Page<Member> getPagedMemberByPlaceId(Long placeId, Pageable pageable) {
        return memberQueryPort.findByPlaceIdWithPageable(placeId, pageable);
    }

    @Override
    public Page<Member> getPageMemberByPlaceIdAndNameContaining(Long placeId, String searchName, Pageable pageable) {
        return memberQueryPort.findByPlaceIdAndNameContaining(placeId, searchName, pageable);
    }

    @Override
    public Member getMemberById(Long memberId) {
        return memberQueryPort.findById(memberId);
    }
}
