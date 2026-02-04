package com.dangbun.domain.member.refactor.application.port.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.exception.custom.*;
import com.dangbun.domain.member.refactor.application.port.in.command.*;
import com.dangbun.domain.member.refactor.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.refactor.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.refactor.domain.Member;
import com.dangbun.domain.memberduty.MemberDuty;
import com.dangbun.domain.memberduty.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.entity.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.member.original.response.status.MemberExceptionResponse.*;

/*
 * TODO: 다른 도메인 헥사고날 아키텍처 전환 시 수정
 * 각 도메인의 Port를 통해 접근하도록 변경 필요
 * - DutyRepository -> DutyQueryPort
 * - MemberDutyRepository -> MemberDutyCommandPort
 */
@RequiredArgsConstructor
@Service
@Transactional
public class MemberCommandService implements MemberCommandUseCase {

    private final MemberCommandPort memberCommandPort;
    private final MemberQueryPort memberQueryPort;
    private final MemberDutyCommandPort memberDutyCommandPort;

    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;

    @Override
    public void registerMember(Long memberId) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        member.activate();
        memberCommandPort.save(member);
    }

    @Override
    public void removeWaitingMember(Long memberId) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        memberCommandPort.delete(member);
    }

    @Override
    public void exitPlace(ExitPlaceCommand command) {
        MemberJpaEntity me = MemberContext.get();

        if (me.getRole() == com.dangbun.domain.member.original.entity.MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        if (!me.getPlace().getName().equals(command.placeName())) {
            throw new NameNotMatchedException(PLACE_NAME_NOT_MATCHED);
        }

        Member member = memberQueryPort.findByMemberIdAndPlaceId(me.getMemberId(), me.getPlace().getPlaceId())
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));

        memberCommandPort.delete(member);
    }

    @Override
    public void removeMember(RemoveMemberCommand command) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Member member = getMemberByMemberIdAndPlaceId(command.memberId(), placeId);

        if (!member.getName().equals(command.memberName())) {
            throw new NameNotMatchedException(NAME_NOT_MATCHED);
        }

        memberCommandPort.delete(member);
    }

    @Override
    public void assignDutyToMember(AssignDutyCommand command) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        Member targetMember = memberQueryPort.findByMemberIdAndPlaceId(command.memberId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));

        /*
         * TODO: Duty 도메인 헥사고날 아키텍처 전환 시 수정
         * DutyRepository -> DutyQueryPort
         */
        Duty duty = dutyRepository.findByDutyIdAndPlace_PlaceId(command.dutyId(), placeId)
                .orElseThrow(() -> new DutyNotInPlaceException(DUTY_NOT_IN_PLACE));

        /*
         * TODO: MemberDuty 도메인 헥사고날 아키텍처 전환 시 수정
         * MemberDutyRepository -> MemberDutyQueryPort, MemberDutyCommandPort
         */
        if (memberDutyRepository.existsByDuty_DutyIdAndMember_MemberId(duty.getDutyId(), targetMember.getMemberId())) {
            throw new MemberDutyAlreadyAssignedException(MEMBER_DUTY_ALREADY_ASSIGNED);
        }

        MemberDuty md = MemberDuty.withIdsBuilder()
                .memberId(targetMember.getMemberId())
                .dutyId(duty.getDutyId())
                .build();

        memberDutyCommandPort.save(md);
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberQueryPort.findByMemberIdAndPlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
    }
}
