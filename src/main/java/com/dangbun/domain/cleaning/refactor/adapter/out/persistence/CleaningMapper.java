package com.dangbun.domain.cleaning.refactor.adapter.out.persistence;

import com.dangbun.domain.cleaning.refactor.adapter.out.CleaningJpaEntity;
import com.dangbun.domain.cleaning.refactor.domain.CleaningRepeatType;
import com.dangbun.domain.cleaning.refactor.domain.Cleaning;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.SpringDataDutyRepository;
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.domain.place.original.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleaningMapper {

    private final SpringDataDutyRepository dutyRepository;

    /*
     * TODO: Place 도메인 헥사고날 아키텍처 전환 시 수정
     * PlaceRepository -> PlaceQueryPort 또는 SpringDataPlaceRepository
     */
    private final PlaceRepository placeRepository;

    public CleaningJpaEntity mapToJpaEntity(Cleaning cleaning) {
        DutyJpaEntity duty = null;
        if (cleaning.getDutyId() != null) {
            duty = dutyRepository.findById(cleaning.getDutyId()).orElse(null);
        }

        Place place = placeRepository.findById(cleaning.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + cleaning.getPlaceId()));

        return CleaningJpaEntity.builder()
                .name(cleaning.getName())
                .repeatType(mapToJpaRepeatType(cleaning.getRepeatType()))
                .repeatDays(cleaning.getRepeatDays())
                .duty(duty)
                .needPhoto(cleaning.getNeedPhoto())
                .place(place)
                .build();
    }

    public Cleaning mapToDomainEntity(CleaningJpaEntity entity) {
        return Cleaning.withId(
                new Cleaning.CleaningId(entity.getCleaningId()),
                entity.getName(),
                mapToDomainRepeatType(entity.getRepeatType()),
                entity.getRepeatDays(),
                entity.getDuty() != null ? entity.getDuty().getDutyId() : null,
                entity.getNeedPhoto(),
                entity.getPlace().getPlaceId()
        );
    }

    private CleaningRepeatType mapToJpaRepeatType(com.dangbun.domain.cleaning.refactor.domain.CleaningRepeatType domainType) {
        if (domainType == null) return null;
        return CleaningRepeatType.valueOf(domainType.name());
    }

    private com.dangbun.domain.cleaning.refactor.domain.CleaningRepeatType mapToDomainRepeatType(CleaningRepeatType jpaType) {
        if (jpaType == null) return null;
        return com.dangbun.domain.cleaning.refactor.domain.CleaningRepeatType.valueOf(jpaType.name());
    }
}
