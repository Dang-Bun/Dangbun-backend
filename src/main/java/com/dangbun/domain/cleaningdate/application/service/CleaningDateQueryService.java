package com.dangbun.domain.cleaningdate.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaningdate.application.port.in.query.GetCleaningDateForCalendarQuery;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateQueryPort;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningDateQueryService implements GetCleaningDateForCalendarQuery {

    private final CleaningDateQueryPort cleaningDateQueryPort;

    @Override
    public List<LocalDate> findDatesByCleaningId(Long cleaningId) {
        return cleaningDateQueryPort.findByCleaningId(cleaningId).stream()
                .map(CleaningDate::getDate)
                .toList();
    }
}
