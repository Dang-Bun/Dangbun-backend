package com.dangbun.domain.memberduty.refactor.adapter.out;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name="member_duty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDutyJpaEntity {
    @EmbeddedId
    private MemberDutyId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MemberJpaEntity member;

    @MapsId("dutyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DutyJpaEntity duty;


    @Builder
    public MemberDutyJpaEntity(MemberJpaEntity member, DutyJpaEntity duty) {
        this.member = member;
        this.duty = duty;
        this.id = new MemberDutyId(member.getMemberId(), duty.getDutyId());
    }

    @Builder(builderMethodName = "withIdsBuilder")
    public MemberDutyJpaEntity(Long memberId, Long dutyId) {
        this.id = new MemberDutyId(memberId, dutyId);
    }
}
