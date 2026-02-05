package com.dangbun.domain.memberduty.refactor;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import lombok.Builder;
import lombok.Getter;

import static com.dangbun.domain.duty.refactor.domain.Duty.*;

public class MemberDuty {

    @Getter
    private MemberDutyId id;

//    @Getter
//    private MemberJpaEntity member;

    @Getter
    private MemberId memberId;

//    @Getter
//    private DutyJpaEntity duty;

    @Getter
    private DutyId dutyId;

    @Builder
    public MemberDuty(MemberJpaEntity member, DutyJpaEntity duty) {
        this.memberId = new MemberId(member.getMemberId());
        this.dutyId = new DutyId(duty.getDutyId());
        this.id = new MemberDutyId(member.getMemberId(), duty.getDutyId());
    }

    @Builder(builderMethodName = "withIdsBuilder")
    public MemberDuty(Long memberId, Long dutyId) {
        this.id = new MemberDutyId(memberId, dutyId);
    }

    public record MemberId(Long value) {
    }

    ;

}
