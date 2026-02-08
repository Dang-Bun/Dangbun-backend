package com.dangbun.domain.cleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Duty 도메인에서 Cleaning 정보 조회를 위한 전용 서비스
 * 순환 참조 방지를 위해 CleaningQueryService에서 분리
 */
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningForDutyQueryService implements GetCleaningForDutyQuery {

    private final CleaningQueryPort cleaningQueryPort;

    @Override
    public List<CleaningInfo> findAllByDutyId(Long dutyId) {
        return cleaningQueryPort.findAllByDutyId(dutyId).stream()
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()))
                .toList();
    }

    @Override
    public List<CleaningInfo> findAllByIds(List<Long> cleaningIds) {
        return cleaningQueryPort.findAllByIds(cleaningIds).stream()
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()))
                .toList();
    }

    @Override
    public Optional<CleaningInfo> findById(Long cleaningId) {
        return cleaningQueryPort.findById(cleaningId)
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()));
    }

    @Override
    public Optional<CleaningInfo> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId) {
        return cleaningQueryPort.findByCleaningIdAndDutyId(cleaningId, dutyId)
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()));
    }
}
