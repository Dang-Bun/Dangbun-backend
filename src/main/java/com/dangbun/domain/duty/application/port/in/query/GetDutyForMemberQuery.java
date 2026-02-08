package com.dangbun.domain.duty.application.port.in.query;

import java.util.Optional;

/**
 * Member 도메인에서 Duty 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetDutyForMemberQuery {

    Optional<DutyInfo> findByIdAndPlaceId(Long dutyId, Long placeId);

    record DutyInfo(Long dutyId, String name) {}
}
