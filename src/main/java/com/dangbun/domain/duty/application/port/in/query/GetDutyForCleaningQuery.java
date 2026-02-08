package com.dangbun.domain.duty.application.port.in.query;

import java.util.List;
import java.util.Optional;

/**
 * Cleaning 도메인에서 Duty 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetDutyForCleaningQuery {

    Optional<DutyInfo> findById(Long dutyId);

    List<DutyInfo> findAll();

    List<DutyInfo> findDistinctDutiesByMemberIds(List<Long> memberIds);

    record DutyInfo(Long dutyId, String name, String icon) {}
}
