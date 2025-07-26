package com.dangbun.domain.membercleaning.entity;


import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="member_cleaning")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCleaning {
    @EmbeddedId
    private MemberCleaningId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @MapsId("cleaningId") // 복합키 클래스의 필드명
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id", nullable = false)
    private Cleaning cleaning;

    @Builder
    public MemberCleaning(Member member, Cleaning cleaning) {
        this.member = member;
        this.cleaning = cleaning;
        this.id = new MemberCleaningId(member.getMemberId(), cleaning.getCleaningId());
    }
}