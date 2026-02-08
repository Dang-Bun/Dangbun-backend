package com.dangbun.domain.cleaning.application.port.in.command;

import java.util.List;

/**
 * Duty 도메인에서 Cleaning 상태 변경을 위한 전용 인커밍 포트
 */
public interface CleaningForDutyUseCase {

    void assignCleaningsToDuty(Long dutyId, List<Long> cleaningIds);

    void removeCleaningFromDuty(Long cleaningId);
}
