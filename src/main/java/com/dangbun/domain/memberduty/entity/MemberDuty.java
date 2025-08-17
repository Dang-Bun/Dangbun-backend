package com.dangbun.domain.memberduty.entity;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
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
public class MemberDuty {
    @EmbeddedId
    private MemberDutyId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @MapsId("dutyId") // 복합키 클래스의 필드명
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Duty duty;


    @Builder
    public MemberDuty(Member member, Duty duty) {
        this.member = member;
        this.duty = duty;
        this.id = new MemberDutyId(member.getMemberId(), duty.getDutyId());
    }
}
