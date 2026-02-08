package com.dangbun.domain.duty.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.duty.application.port.in.query.GetDutyForCleaningQuery;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Cleaning 도메인에서 Duty 정보 조회를 위한 전용 서비스
 * 순환 참조 방지를 위해 DutyQueryService에서 분리
 */
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DutyForCleaningQueryService implements GetDutyForCleaningQuery {

    private final DutyQueryPort dutyQueryPort;

    @Override
    public Optional<DutyInfo> findById(Long dutyId) {
        return dutyQueryPort.findById(dutyId)
                .map(duty -> new DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ));
    }

    @Override
    public List<DutyInfo> findAll() {
        return dutyQueryPort.findAll().stream()
                .map(duty -> new DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ))
                .toList();
    }

    @Override
    public List<DutyInfo> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        return dutyQueryPort.findDistinctDutiesByMemberIds(memberIds).stream()
                .map(duty -> new DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ))
                .toList();
    }
}
