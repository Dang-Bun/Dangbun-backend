package com.dangbun.domain.membercleaning.adapter.out.persistence;


import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
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
public class MemberCleaningJpaEntity {
    @EmbeddedId
    private MemberCleaningId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MemberJpaEntity member;

    @MapsId("cleaningId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CleaningJpaEntity cleaningJpaEntity;

    @Builder
    public MemberCleaningJpaEntity(MemberJpaEntity member, CleaningJpaEntity cleaningJpaEntity) {
        this.member = member;
        this.cleaningJpaEntity = cleaningJpaEntity;
        this.id = new MemberCleaningId(member.getMemberId(), cleaningJpaEntity.getCleaningId());
    }
}