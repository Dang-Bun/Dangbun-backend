package com.dangbun.domain.cleaningdate.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaningdate.application.port.in.command.CleaningDateForCleaningUseCase;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CleaningDateCommandService implements CleaningDateForCleaningUseCase {

    private final CleaningDateCommandPort cleaningDateCommandPort;

    @Override
    public void saveAllByCleaningId(Long cleaningId, List<LocalDate> dates) {
        List<CleaningDate> cleaningDates = dates.stream()
                .map(date -> CleaningDate.withoutId(date, cleaningId))
                .toList();

        cleaningDateCommandPort.saveAll(cleaningDates);
    }

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        cleaningDateCommandPort.deleteAllByCleaningId(cleaningId);
    }
}
