package com.dangbun.domain.member.entity;

import com.dangbun.domain.duty.entity.DutyIcon;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(nullable = false)
    private Boolean status;

    @Column(columnDefinition = "json")
    private String information;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Member(MemberRole role, Boolean status, String information, Place place, User user) {
       this.role = role;
       this.status = status;
       this.information = information;
       this.place = place;
       this.user = user;
    }

}
