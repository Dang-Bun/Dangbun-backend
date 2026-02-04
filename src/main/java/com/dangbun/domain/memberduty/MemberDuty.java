package com.dangbun.domain.memberduty;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

public class MemberDuty {

    @Getter
    private MemberDutyId id;

    @Getter
    private MemberJpaEntity member;

    @Getter
    private Duty duty;


    @Builder
    public MemberDuty(MemberJpaEntity member, Duty duty) {
        this.member = member;
        this.duty = duty;
        this.id = new MemberDutyId(member.getMemberId(), duty.getDutyId());
    }

    @Builder(builderMethodName = "withIdsBuilder")
    public MemberDuty(Long memberId, Long dutyId) {
        this.id = new MemberDutyId(memberId, dutyId);
    }
}
