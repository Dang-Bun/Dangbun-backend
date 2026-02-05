package com.dangbun.domain.duty.refactor.adapter.out.persistence;

import com.dangbun.domain.duty.refactor.domain.DutyIcon;
import com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "duty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DutyJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duty_id")
    private Long dutyId;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DutyIcon icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlaceJpaEntity place;

    @Builder
    public DutyJpaEntity(Long dutyId, String name, DutyIcon icon, PlaceJpaEntity place) {
        this.dutyId = dutyId;
        this.name = name;
        this.icon = icon;
        this.place = place;
    }

    public void update(String name, DutyIcon icon) {
        this.name = name;
        this.icon = icon;
    }
}
