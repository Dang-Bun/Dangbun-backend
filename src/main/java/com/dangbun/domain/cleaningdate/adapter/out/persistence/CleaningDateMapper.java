package com.dangbun.domain.cleaningdate.adapter.out.persistence;

import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleaningDateMapper {

    private final CleaningRepository cleaningRepository;

    public CleaningDateJpaEntity mapToJpaEntity(CleaningDate cleaningDate) {
        CleaningJpaEntity cleaning = cleaningRepository.findById(cleaningDate.getCleaningId())
                .orElseThrow(() -> new IllegalArgumentException("Cleaning not found: " + cleaningDate.getCleaningId()));

        return CleaningDateJpaEntity.builder()
                .date(cleaningDate.getDate())
                .cleaningJpaEntity(cleaning)
                .build();
    }

    public CleaningDate mapToDomainEntity(CleaningDateJpaEntity entity) {
        return CleaningDate.withId(
                new CleaningDate.CleaningDateId(entity.getCleaningDateId()),
                entity.getDate(),
                entity.getCleaningJpaEntity().getCleaningId()
        );
    }
}
