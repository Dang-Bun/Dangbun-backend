package com.dangbun.domain.memberduty.entity;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDuty {
    @EmbeddedId
    private MemberDutyId id;

    @MapsId("dutyId") // 복합키 클래스의 필드명
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id", nullable = false)
    private Duty duty;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public MemberDuty(Duty duty, Member member) {
        this.duty = duty;
        this.member = member;
    }
}
