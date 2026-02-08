package com.dangbun.domain.cleaning.adapter.out.persistence;

import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.entity.Place
 *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
 * - place 필드 타입 변경: Place -> PlaceJpaEntity
 * - @ManyToOne 관계 유지, JoinColumn 동일
 * - Builder 파라미터 타입도 함께 변경
 */
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
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
@Table(name = "cleaning")
public class CleaningJpaEntity extends BaseEntity {
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
    private DutyJpaEntity duty;

    @Column(name = "need_photo", nullable = false)
    private Boolean needPhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlaceJpaEntity place;

    @Builder
    public CleaningJpaEntity(Long cleaningId, String name, CleaningRepeatType repeatType, String repeatDays, DutyJpaEntity duty, Boolean needPhoto, PlaceJpaEntity place) {
        this.cleaningId = cleaningId;
        this.name = name;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
        this.needPhoto = needPhoto;
        this.place = place;
    }

    public void updateInfo(String name, Boolean needPhoto, CleaningRepeatType repeatType, String repeatDays, DutyJpaEntity duty) {
        this.name = name;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
        this.needPhoto = needPhoto;
    }

    public void assignToDuty(DutyJpaEntity duty) {
        this.duty = duty;
    }

    public void removeDuty() {
        this.duty = null;
    }
}
