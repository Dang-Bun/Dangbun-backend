package com.dangbun.domain.member.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.dto.response.GetMembersResponse;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDutyRepository memberDutyRepository;


    @Transactional(readOnly = true)
    public GetMembersResponse getMembers(Long placeId) {
        Map<Member, List<String>> memberMap = new HashMap<>();
        List<Member> members = memberRepository.findByPlaceId(placeId);
        for(Member member : members){
            List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
            List<String> dutyNames = new ArrayList<>();
            for(MemberDuty memberDuty : memberDuties){
                dutyNames.add(memberDuty.getDuty().getName());
            }
            memberMap.put(member,dutyNames);
        }
        return GetMembersResponse.of(memberMap);
    }


}
