package com.dangbun.domain.cleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForChecklistQuery;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Checklist 도메인에서 Cleaning 정보 조회를 위한 전용 서비스
 */
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningForChecklistQueryService implements GetCleaningForChecklistQuery {

    private final CleaningQueryPort cleaningQueryPort;

    @Override
    public List<CleaningInfo> findAll() {
        return cleaningQueryPort.findAll().stream()
                .map(c -> new CleaningInfo(
                        c.getCleaningId().value(),
                        c.getName(),
                        c.getRepeatType(),
                        c.getRepeatDays(),
                        c.getNeedPhoto(),
                        c.getPlaceId()
                ))
                .toList();
    }

    @Override
    public Optional<CleaningInfo> findById(Long cleaningId) {
        return cleaningQueryPort.findById(cleaningId)
                .map(c -> new CleaningInfo(
                        c.getCleaningId().value(),
                        c.getName(),
                        c.getRepeatType(),
                        c.getRepeatDays(),
                        c.getNeedPhoto(),
                        c.getPlaceId()
                ));
    }
}
