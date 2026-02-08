package com.dangbun.domain.memberduty.application.port.in.command;

/**
 * Member 도메인에서 MemberDuty 상태 변경을 위한 전용 인커밍 포트
 */
public interface MemberDutyForMemberUseCase {

    void saveMemberDuty(Long memberId, Long dutyId);
}
