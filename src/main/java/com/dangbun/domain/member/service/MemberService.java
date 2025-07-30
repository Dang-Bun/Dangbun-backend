package com.dangbun.domain.member.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.dto.request.*;
import com.dangbun.domain.member.dto.response.*;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.firewall.RequestRejectedException;
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
    private final MemberCleaningRepository memberCleaningRepository;


    @Transactional(readOnly = true)
    public GetMembersResponse getMembers(User user, Long placeId) {

        Member me = memberRepository.findByUser_UserIdAndPlace_PlaceId(user.getUserId(), placeId)
                .orElseThrow(()->new MemberNotFoundException(NO_SUCH_MEMBER));

        Map<Member, List<String>> memberMap = new HashMap<>();

        List<Member> members = memberRepository.findByPlace_PlaceId(placeId);

        Integer waitingMemberNumber = 0;

        for (Member member : members) {
            if(member.getStatus()) {
                List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
                List<String> dutyNames = new ArrayList<>();
                for (MemberDuty memberDuty : memberDuties) {
                    dutyNames.add(memberDuty.getDuty().getName());
                }
                memberMap.put(member, dutyNames);
            }
            if(!member.getStatus()){
                waitingMemberNumber++;
            }
        }

        if(me.getRole().equals(MemberRole.MEMBER)){
            waitingMemberNumber = null;
        }

        return GetMembersResponse.of(waitingMemberNumber, memberMap);
    }


    @Transactional(readOnly = true)
    public GetMemberResponse getMember(Long placeId, Long memberId) {
        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

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

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsFalse(placeId);

        return GetWaitingMembersResponse.of(members);

    }

    public void registerMember(User user, Long placeId, Long memberId) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

        member.activate();
    }

    public void removeWaitingMember(User user, Long placeId, Long memberId) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }
        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

        memberRepository.delete(member);
    }

    public void exitPlace(User user, Long placeId, DeleteSelfFromPlaceRequest request) {
        Member member = memberRepository.findByUser_UserIdAndPlace_PlaceId(user.getUserId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));

        if (member.getRole() == MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        memberRepository.delete(member);
    }

    public void removeMember(User user, Long placeId, Long memberId, DeleteMemberRequest request) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(()-> new MemberNotFoundException(NO_SUCH_MEMBER));

        if(!member.getName().equals(request.memberName())){
            throw new NameNotMatchedException(NAME_NOT_MATCHED);
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
        memberDutyRepository.deleteAll(memberDuties);

        memberRepository.delete(member);
    }

    private Member getMemberByUserAndPlace(Long userId, Long placeId) {
        return memberRepository.findByUser_UserIdAndPlace_PlaceId(userId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId){
        return memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));
    }

    public GetMemberSearchResponse searchByNameInPlace(User user, Long placeId, String name) {
        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        return memberRepository.findByPlace_PlaceIdAndName(placeId, name)
            .map(member -> GetMemberSearchResponse.of(member.getMemberId(), member.getName()))
                    .orElse(GetMemberSearchResponse.of(null, null));
    }

    /**
     * 해당 함수는 연관된 클래스에서 Member 클래스를 삭제하기 위해 호출하는 함수임
     * 탈퇴 및 추방 등 Member 도메인에서 직접 호출하는 로직 같은 경우는 removeX 메서드를 사용
     */
    public void deleteMember(Member member) {
        memberCleaningRepository.deleteAllByMember(member);
        memberDutyRepository.deleteAllByMember(member);

        memberRepository.delete(member);
    }
}
