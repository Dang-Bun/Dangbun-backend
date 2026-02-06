package com.dangbun.domain.memberduty.application.port.out;

import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.memberduty.domain.MemberDuty;

import java.util.List;

public interface MemberDutyQueryPort {

    List<MemberDuty> findAllByPlaceId(Long placeId);

    List<MemberDuty> findAllByDutyId(Long dutyId);

    List<MemberDuty> findAllByMemberId(Long memberId);

    List<Long> findMemberIdsByDutyId(Long dutyId);

    boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId);

    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);
}
