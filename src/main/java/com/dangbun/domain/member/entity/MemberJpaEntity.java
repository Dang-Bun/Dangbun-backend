package com.dangbun.domain.member.entity;

/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.entity.Place
 *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
 * - place 필드 타입 변경: Place -> PlaceJpaEntity
 * - @ManyToOne 관계 유지, JoinColumn 동일
 * - Builder 파라미터 타입도 함께 변경
 */
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.domain.user.entity.User;
import com.dangbun.global.entity.BaseEntity;
import com.dangbun.global.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Map;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity extends BaseEntity {
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public MemberJpaEntity(MemberRole role, String name, Boolean status, Map information, Place place, User user) {
       this.role = role;
       this.name = name;
       this.status = status;
       this.information = information;
       this.place = place;
       this.user = user;
    }

    public void activate(){
        this.role = MemberRole.MEMBER;
        this.status = true;
    }

}
