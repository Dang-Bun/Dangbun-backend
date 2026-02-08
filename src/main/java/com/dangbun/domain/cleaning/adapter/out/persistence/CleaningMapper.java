package com.dangbun.domain.cleaning.adapter.out.persistence;

import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.adapter.out.persistence.SpringDataDutyRepository;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.adapter.out.persistence.SpringDataPlaceRepository;
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
    private final SpringDataPlaceRepository placeRepository;

    public CleaningJpaEntity mapToJpaEntity(Cleaning cleaning) {
        DutyJpaEntity duty = null;
        if (cleaning.getDutyId() != null) {
            duty = dutyRepository.findById(cleaning.getDutyId()).orElse(null);
        }

        PlaceJpaEntity place = placeRepository.findById(cleaning.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + cleaning.getPlaceId()));

        CleaningJpaEntity.CleaningJpaEntityBuilder builder = CleaningJpaEntity.builder()
                .name(cleaning.getName())
                .repeatType(mapToJpaRepeatType(cleaning.getRepeatType()))
                .repeatDays(cleaning.getRepeatDays())
                .duty(duty)
                .needPhoto(cleaning.getNeedPhoto())
                .place(place);

        if (cleaning.getCleaningId() != null) {
            builder.cleaningId(cleaning.getCleaningId().value());
        }

        return builder.build();
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

    private CleaningRepeatType mapToJpaRepeatType(CleaningRepeatType domainType) {
        if (domainType == null) return null;
        return CleaningRepeatType.valueOf(domainType.name());
    }

    private CleaningRepeatType mapToDomainRepeatType(CleaningRepeatType jpaType) {
        if (jpaType == null) return null;
        return CleaningRepeatType.valueOf(jpaType.name());
    }
}
