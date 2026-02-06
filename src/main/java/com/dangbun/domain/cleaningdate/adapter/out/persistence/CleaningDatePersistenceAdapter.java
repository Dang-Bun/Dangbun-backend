package com.dangbun.domain.cleaningdate.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class CleaningDatePersistenceAdapter implements CleaningDateCommandPort {

    private final CleaningDateRepository cleaningDateRepository;

    private final CleaningDateMapper cleaningDateMapper;

    @Override
    public void saveAll(List<CleaningDate> cleaningDates) {

        List<CleaningDateJpaEntity> jpaEntities = cleaningDates.stream().map(cleaningDateMapper::mapToJpaEntity).toList();
        cleaningDateRepository.saveAll(jpaEntities);

    }

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        cleaningDateRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningId);
    }
}
