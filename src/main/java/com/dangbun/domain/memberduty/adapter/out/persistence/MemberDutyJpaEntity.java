package com.dangbun.domain.memberduty.adapter.out.persistence;

import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "member_duty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDutyJpaEntity {

    @EmbeddedId
    private MemberDutyJpaEntityId id;

    /*
     * TODO: Member 도메인 헥사고날 아키텍처 전환 완료 후 수정
     * MemberJpaEntity -> refactor 패키지의 MemberJpaEntity
     */
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
        this.id = new MemberDutyJpaEntityId(member.getMemberId(), duty.getDutyId());
    }
}
