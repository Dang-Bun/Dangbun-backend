package com.dangbun.domain.memberduty.application.port.out;

import com.dangbun.domain.memberduty.domain.MemberDuty;

import java.util.List;

public interface MemberDutyCommandPort {

    void save(MemberDuty memberDuty);

    void saveAll(List<MemberDuty> memberDuties);

    void deleteAllByDutyId(Long dutyId);

    void deleteByMemberIdAndDutyId(Long memberId, Long dutyId);
}
