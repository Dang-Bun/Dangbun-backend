package com.dangbun.domain.member.entity;

import com.dangbun.domain.duty.entity.DutyIcon;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.user.entity.User;
import com.dangbun.global.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

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
    private String name;

    @Column(nullable = false)
    private Boolean status;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, String> information;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Member(MemberRole role,String name, Boolean status, Map information, Place place, User user) {
       this.role = role;
       this.name = name;
       this.status = status;
       this.information = information;
       this.place = place;
       this.user = user;
    }

}
