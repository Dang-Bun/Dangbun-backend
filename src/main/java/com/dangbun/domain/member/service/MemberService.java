package com.dangbun.domain.member.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.dto.request.*;
import com.dangbun.domain.member.dto.response.*;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;

    @Transactional(readOnly = true)
    public GetMembersResponse getMembers() {

        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Map<Member, List<String>> memberMap = new HashMap<>();

        List<Member> members = memberRepository.findByPlace_PlaceId(placeId);

        Integer waitingMemberNumber = 0;

        for (Member member : members) {
            if (member.getStatus()) {
                List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
                List<String> dutyNames = new ArrayList<>();
                for (MemberDuty memberDuty : memberDuties) {
                    dutyNames.add(memberDuty.getDuty().getName());
                }
                memberMap.put(member, dutyNames);
            }
            if (!member.getStatus()) {
                waitingMemberNumber++;
            }
        }

        if (me.getRole().equals(MemberRole.MEMBER)) {
            waitingMemberNumber = null;
        }

        return GetMembersResponse.of(waitingMemberNumber, memberMap);
    }


    @Transactional(readOnly = true)
    public GetMemberResponse getMember( Long memberId) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();
        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
        List<Duty> duties = new ArrayList<>();
        for (MemberDuty memberDuty : memberDuties) {
            Duty duty = memberDuty.getDuty();
            duties.add(duty);
        }

        return GetMemberResponse.of(member, duties);
    }

    @Transactional(readOnly = true)
    public GetWaitingMembersResponse getWaitingMembers() {
        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsFalse(placeId);

        return GetWaitingMembersResponse.of(members);

    }

    public void registerMember(Long memberId) {
        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();


        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        member.activate();
    }

    public void removeWaitingMember(Long memberId) {
        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        memberRepository.delete(member);
    }

    public void exitPlace(DeleteSelfFromPlaceRequest request) {
        Member me = MemberContext.get();

        if (me.getRole() == MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        if (!me.getPlace().getName().equals(request.placeName())) {
            throw new NameNotMatchedException(PLACE_NAME_NOT_MATCHED);
        }

        memberRepository.delete(me);
    }

    public void removeMember(Long memberId, DeleteMemberRequest request) {

        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Member member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        if (!member.getName().equals(request.memberName())) {
            throw new NameNotMatchedException(NAME_NOT_MATCHED);
        }

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public GetMyInformationResponse getMyInformation() {
        Member me = MemberContext.get();
        return GetMyInformationResponse.of(me);

    }

    @Transactional(readOnly = true)
    public GetMemberSearchResponse searchByNameInPlace(Long placeId, String name) {
        return memberRepository.findByPlace_PlaceIdAndName(placeId, name)
                .map(GetMemberSearchResponse::of)
                .orElse(new GetMemberSearchResponse(null, null));
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
    }


    public void assignDutyToMember(Long memberId, Long dutyId) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        Member targetMember = memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));

        Duty duty = dutyRepository.findByDutyIdAndPlace_PlaceId(dutyId, placeId)
                .orElseThrow(() -> new DutyNotInPlaceException(DUTY_NOT_IN_PLACE));

        if (memberDutyRepository.existsByDutyAndMember(duty, targetMember)) {
            throw new MemberDutyAlreadyAssignedException(MEMBER_DUTY_ALREADY_ASSIGNED);
        }

        MemberDuty memberDuty = MemberDuty.builder()
                .member(targetMember)
                .duty(duty)
                .build();

        memberDutyRepository.save(memberDuty);
    }
}
