package com.dangbun.domain.memberduty.refactor;

import com.dangbun.domain.duty.refactor.domain.Duty;

import java.util.List;

public interface MemberDutyQueryPort {
    List<MemberDuty> findAllWithMemberAndPlaceByPlaceId(Long placeId);

}
