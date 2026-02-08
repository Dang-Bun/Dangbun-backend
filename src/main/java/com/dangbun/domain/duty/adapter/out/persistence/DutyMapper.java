package com.dangbun.domain.duty.adapter.out.persistence;

import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.adapter.out.persistence.SpringDataPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DutyMapper {

    private final SpringDataPlaceRepository placeRepository;

    public DutyJpaEntity mapToJpaEntity(Duty duty) {
        PlaceJpaEntity place = placeRepository.findById(duty.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + duty.getPlaceId()));

        return DutyJpaEntity.builder()
                .dutyId(duty.getDutyId() != null ? duty.getDutyId().value() : null)
                .name(duty.getName())
                .icon(duty.getIcon())
                .place(place)
                .build();
    }

    public Duty mapToDomainEntity(DutyJpaEntity entity) {
        return Duty.withId(
                new Duty.DutyId(entity.getDutyId()),
                entity.getName(),
                entity.getIcon(),
                entity.getPlace().getPlaceId()
        );
    }
}
