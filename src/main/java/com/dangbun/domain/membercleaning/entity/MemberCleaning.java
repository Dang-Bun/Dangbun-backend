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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @MapsId("cleaningId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cleaning cleaning;

    @Builder
    public MemberCleaning(Member member, Cleaning cleaning) {
        this.member = member;
        this.cleaning = cleaning;
        this.id = new MemberCleaningId(member.getMemberId(), cleaning.getCleaningId());
    }
}