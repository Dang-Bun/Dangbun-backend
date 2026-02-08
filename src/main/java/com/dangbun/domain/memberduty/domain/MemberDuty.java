package com.dangbun.domain.memberduty.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MemberDuty {

    private final MemberDutyId memberDutyId;
    private final Long memberId;
    private final Long dutyId;

    public static MemberDuty of(Long memberId, Long dutyId) {
        return new MemberDuty(new MemberDutyId(memberId, dutyId), memberId, dutyId);
    }

    public record MemberDutyId(Long memberId, Long dutyId) {
    }
}
