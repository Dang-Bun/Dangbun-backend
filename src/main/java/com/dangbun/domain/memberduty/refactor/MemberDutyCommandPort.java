package com.dangbun.domain.memberduty.refactor;

public interface MemberDutyCommandPort {
    public void save(MemberDuty memberDuty);
    void deleteAllByDutyId(Long dutyId);
}
