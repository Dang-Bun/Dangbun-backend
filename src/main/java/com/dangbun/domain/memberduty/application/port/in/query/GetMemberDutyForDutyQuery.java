package com.dangbun.domain.memberduty.application.port.in.query;

import java.util.List;

/**
 * Duty 도메인에서 MemberDuty 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMemberDutyForDutyQuery {

    List<Long> findMemberIdsByDutyId(Long dutyId);

    List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId);

    record MemberDutyMemberInfo(Long memberId, String role, String name) {}
}
