package com.dangbun.domain.memberduty.application.port.out;

import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.memberduty.domain.MemberDuty;

import java.util.List;

public interface MemberDutyQueryPort {

    List<MemberDuty> findAllByPlaceId(Long placeId);

    List<MemberDuty> findAllByDutyId(Long dutyId);

    List<MemberDuty> findAllByMemberId(Long memberId);

    List<Long> findMemberIdsByDutyId(Long dutyId);

    boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId);

    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);

    /**
     * Duty에 할당된 멤버 정보 조회 (Duty 도메인에서 사용)
     */
    List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId);

    record MemberDutyMemberInfo(Long memberId, String role, String name) {}
}
