package com.dangbun.domain.member.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.dto.response.GetMemberResponse;
import com.dangbun.domain.member.dto.response.GetMembersResponse;
import com.dangbun.domain.member.dto.response.GetWaitingMembersResponse;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
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


    @Transactional(readOnly = true)
    public GetMembersResponse getMembers(Long placeId) {

        Map<Member, List<String>> memberMap = new HashMap<>();

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsTrue(placeId);
        for (Member member : members) {
            List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
            List<String> dutyNames = new ArrayList<>();
            for (MemberDuty memberDuty : memberDuties) {
                dutyNames.add(memberDuty.getDuty().getName());
            }
            memberMap.put(member, dutyNames);
        }
        return GetMembersResponse.of(memberMap);
    }


    @Transactional(readOnly = true)
    public GetMemberResponse getMember(Long placeId, Long memberId) {
        Member member = memberRepository.findByPlace_PlaceIdAndMemberId(placeId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 맴버가 존재하지 않습니다"));

        List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
        List<Duty> duties = new ArrayList<>();
        for (MemberDuty memberDuty : memberDuties) {
            Duty duty = memberDuty.getDuty();
            duties.add(duty);
        }

        return GetMemberResponse.of(member, duties);
    }

    @Transactional(readOnly = true)
    public GetWaitingMembersResponse getWaitingMembers(User user, Long placeId) {
        checkMemberRoleWithUserAndPlaceId(user, placeId);

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsFalse(placeId);

        return GetWaitingMembersResponse.of(members);

    }

    public void registerMember(User user, Long placeId, Long memberId) {
        checkMemberRoleWithUserAndPlaceId(user, placeId);

        Member member = memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId).orElseThrow(
                () -> new NoSuchElementException("해당하는 맴버가 존재하지 않습니다."));
        member.activate();
    }

    private void checkMemberRoleWithUserAndPlaceId(User user, Long placeId) {
        Member runner = memberRepository.findByUser_IdAndPlace_PlaceId(user.getId(), placeId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 맴버가 존재하지 않습니다."));
        if (!runner.getRole().equals(MemberRole.MANAGER)) {
            throw new InvalidRoleException(INVALID_ROLE);
        }
    }


}
