package com.dangbun.domain.cleaning.application.port.in.query;

import java.util.List;
import java.util.Optional;

/**
 * Duty 도메인에서 Cleaning 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetCleaningForDutyQuery {

    List<CleaningInfo> findAllByDutyId(Long dutyId);

    List<CleaningInfo> findAllByIds(List<Long> cleaningIds);

    Optional<CleaningInfo> findById(Long cleaningId);

    Optional<CleaningInfo> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId);

    record CleaningInfo(Long cleaningId, String name, Long dutyId) {}
}
