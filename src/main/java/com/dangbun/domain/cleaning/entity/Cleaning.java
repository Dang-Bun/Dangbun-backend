package com.dangbun.domain.cleaning.entity;

import com.dangbun.domain.duty.entity.Duty;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.entity.Place
 *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
 * - place 필드 타입 변경: Place -> PlaceJpaEntity
 * - @ManyToOne 관계 유지, JoinColumn 동일
 * - Builder 파라미터 타입도 함께 변경
 */
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cleaning extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleaning_id")
    private Long cleaningId;

    @Column(nullable = false, length = 20, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private CleaningRepeatType repeatType;

    @Column(name = "repeat_days", length = 20)
    private String repeatDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id")
    private Duty duty;

    @Column(name = "need_photo", nullable = false)
    private Boolean needPhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Place place;

    @Builder
    public Cleaning(String name, CleaningRepeatType repeatType, String repeatDays, Duty duty, Boolean needPhoto, Place place) {
        this.name = name;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
        this.needPhoto = needPhoto;
        this.place = place;
    }

    public void updateInfo(String name, Boolean needPhoto, CleaningRepeatType repeatType, String repeatDays, Duty duty) {
        this.name = name;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
        this.needPhoto = needPhoto;
    }

    public void assignToDuty(Duty duty) {
        this.duty = duty;
    }

    public void removeDuty() {
        this.duty = null;
    }
}
