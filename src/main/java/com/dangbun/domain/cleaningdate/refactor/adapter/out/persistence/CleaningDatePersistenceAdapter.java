package com.dangbun.domain.cleaningdate.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.cleaningdate.entity.CleaningDateJpaEntity;
import com.dangbun.domain.cleaningdate.refactor.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.cleaningdate.refactor.domain.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
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
}
