package com.dangbun.domain.member.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.duty.application.port.in.query.GetDutyForMemberQuery;
import com.dangbun.domain.duty.application.port.in.query.GetDutyForMemberQuery.DutyInfo;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.command.AssignDutyCommand;
import com.dangbun.domain.member.application.port.in.command.ExitPlaceCommand;
import com.dangbun.domain.member.application.port.in.command.MemberCommandUseCase;
import com.dangbun.domain.member.application.port.in.command.RemoveMemberCommand;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.memberduty.application.port.in.command.MemberDutyForMemberUseCase;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForMemberQuery;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.member.exception.status.MemberExceptionResponse.*;

@UseCase
@RequiredArgsConstructor
@Transactional
public class MemberCommandService implements MemberCommandUseCase {

    private final MemberCommandPort memberCommandPort;
    private final MemberQueryPort memberQueryPort;
    private final GetDutyForMemberQuery getDutyForMemberQuery;
    private final GetMemberDutyForMemberQuery getMemberDutyForMemberQuery;
    private final MemberDutyForMemberUseCase memberDutyForMemberUseCase;

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

        if (me.getRole() == MemberRole.MANAGER) {
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

        DutyInfo duty = getDutyForMemberQuery.findByIdAndPlaceId(command.dutyId(), placeId)
                .orElseThrow(() -> new DutyNotInPlaceException(DUTY_NOT_IN_PLACE));

        if (getMemberDutyForMemberQuery.existsByDutyIdAndMemberId(duty.dutyId(), targetMember.getMemberId())) {
            throw new MemberDutyAlreadyAssignedException(MEMBER_DUTY_ALREADY_ASSIGNED);
        }

        memberDutyForMemberUseCase.saveMemberDuty(targetMember.getMemberId(), duty.dutyId());
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberQueryPort.findByMemberIdAndPlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
    }
}
