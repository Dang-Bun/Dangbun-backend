package com.dangbun.domain.member.original.service;

import com.dangbun.domain.duty.original.repository.DutyRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.original.dto.request.DeleteMemberRequest;
import com.dangbun.domain.member.original.dto.request.DeleteSelfFromPlaceRequest;
import com.dangbun.domain.member.original.dto.response.*;
import com.dangbun.domain.member.original.exception.custom.*;
import com.dangbun.global.context.MemberContext;

import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.memberduty.refactor.adapter.out.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.member.original.response.status.MemberExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;

    @Transactional(readOnly = true)
    public GetMembersResponse getMembers() {

        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        Map<MemberJpaEntity, List<String>> memberMap = new LinkedHashMap<>();
        List<MemberJpaEntity> members = memberRepository.findByPlace_PlaceId(placeId);


        members.sort(Comparator
                .comparing((MemberJpaEntity m) -> m.getRole() != MemberRole.MANAGER) // MANAGER 먼저
                .thenComparing(MemberJpaEntity::getName, Comparator.nullsLast(String::compareTo))); // 이름 가나다순

        Integer waitingMemberNumber = 0;

        for (MemberJpaEntity member : members) {
            if (member.getStatus()) {
                List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllByMember(member);
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

        if (me.getRole().equals(MemberRole.MEMBER)) {
            waitingMemberNumber = null;
        }

        return GetMembersResponse.of(waitingMemberNumber, memberMap);
    }


    @Transactional(readOnly = true)
    public GetMemberResponse getMember(Long memberId) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();
        MemberJpaEntity member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllByMember(member);
        List<DutyJpaEntity> duties = new ArrayList<>();
        for (MemberDutyJpaEntity memberDutyJpaEntity : memberDuties) {
            DutyJpaEntity duty = memberDutyJpaEntity.getDuty();
            duties.add(duty);
        }

        return GetMemberResponse.of(member, duties);
    }

    @Transactional(readOnly = true)
    public GetWaitingMembersResponse getWaitingMembers() {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<MemberJpaEntity> members = memberRepository.findByPlace_PlaceIdAndStatusIsFalseOrderByNameAsc(placeId);

        return GetWaitingMembersResponse.of(members);

    }

    public void registerMember(Long memberId) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();


        MemberJpaEntity member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        member.activate();
    }

    public void removeWaitingMember(Long memberId) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        MemberJpaEntity member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        memberRepository.delete(member);
    }

    public void exitPlace(DeleteSelfFromPlaceRequest request) {
        MemberJpaEntity me = MemberContext.get();

        if (me.getRole() == MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        if (!me.getPlace().getName().equals(request.placeName())) {
            throw new NameNotMatchedException(PLACE_NAME_NOT_MATCHED);
        }

        memberRepository.delete(me);
    }

    public void removeMember(Long memberId, DeleteMemberRequest request) {

        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        MemberJpaEntity member = getMemberByMemberIdAndPlaceId(memberId, placeId);

        if (!member.getName().equals(request.memberName())) {
            throw new NameNotMatchedException(NAME_NOT_MATCHED);
        }

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public GetMyInformationResponse getMyInformation() {
        MemberJpaEntity me = MemberContext.get();
        return GetMyInformationResponse.of(me);

    }

    @Transactional(readOnly = true)
    public GetMemberSearchResponse searchByNameInPlace(Long placeId, String name) {
        return memberRepository.findByPlace_PlaceIdAndName(placeId, name)
                .map(GetMemberSearchResponse::of)
                .orElse(new GetMemberSearchResponse(null, null));
    }

    private MemberJpaEntity getMemberByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));
    }


    public void assignDutyToMember(Long memberId, Long dutyId) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        MemberJpaEntity targetMember = memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(MEMBER_NOT_FOUND));

        DutyJpaEntity duty = dutyRepository.findByDutyIdAndPlace_PlaceId(dutyId, placeId)
                .orElseThrow(() -> new DutyNotInPlaceException(DUTY_NOT_IN_PLACE));

        if (memberDutyRepository.existsByDutyAndMember(duty, targetMember)) {
            throw new MemberDutyAlreadyAssignedException(MEMBER_DUTY_ALREADY_ASSIGNED);
        }

        MemberDutyJpaEntity memberDutyJpaEntity = MemberDutyJpaEntity.builder()
                .member(targetMember)
                .duty(duty)
                .build();

        memberDutyRepository.save(memberDutyJpaEntity);
    }
}
