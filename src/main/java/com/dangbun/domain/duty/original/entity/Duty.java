//package com.dangbun.domain.duty.original.entity;
//
///*
// * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
// * - import 변경: com.dangbun.domain.place.original.entity.Place
// *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
// * - place 필드 타입 변경: Place -> PlaceJpaEntity
// * - @ManyToOne 관계 유지, JoinColumn 동일
// * - Builder 파라미터 타입도 함께 변경
// */
//import com.dangbun.domain.place.original.entity.Place;
//import com.dangbun.global.entity.BaseEntity;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
//
//@Entity
//@Table(name = "duty")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class Duty extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "duty_id")
//    private Long dutyId;
//
//    @Column(nullable = false, length = 20)
//    private String name;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 30)
//    private DutyIcon icon;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "place_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private Place place;
//
//    @Builder
//    public Duty(String name, DutyIcon icon, Place place) {
//        this.name = name;
//        this.icon = icon;
//        this.place = place;
//    }
//
//    public void update(String name, DutyIcon icon) {
//        this.name = name;
//        this.icon = icon;
//    }
//
//}
