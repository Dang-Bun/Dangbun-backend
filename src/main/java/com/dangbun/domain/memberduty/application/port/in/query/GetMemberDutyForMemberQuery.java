package com.dangbun.domain.memberduty.application.port.in.query;

import java.util.List;

/**
 * Member 도메인에서 MemberDuty 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMemberDutyForMemberQuery {

    List<DutyInfo> findDutyInfosByMemberId(Long memberId);

    boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId);

    record DutyInfo(Long dutyId, String dutyName) {}
}
