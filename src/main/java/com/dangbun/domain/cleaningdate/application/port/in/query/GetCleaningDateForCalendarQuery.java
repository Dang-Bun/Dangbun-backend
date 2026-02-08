package com.dangbun.domain.cleaningdate.application.port.in.query;

import java.time.LocalDate;
import java.util.List;

/**
 * Calendar 도메인에서 CleaningDate 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetCleaningDateForCalendarQuery {

    List<LocalDate> findDatesByCleaningId(Long cleaningId);
}
