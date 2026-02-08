package com.dangbun.domain.cleaningdate.application.port.in.command;

import java.time.LocalDate;
import java.util.List;

/**
 * Cleaning 도메인에서 CleaningDate 상태 변경을 위한 전용 인커밍 포트
 */
public interface CleaningDateForCleaningUseCase {

    void saveAllByCleaningId(Long cleaningId, List<LocalDate> dates);

    void deleteAllByCleaningId(Long cleaningId);
}
