package com.dangbun.domain.memberduty.application.port.in.command;

import java.util.List;

/**
 * Duty 도메인에서 MemberDuty 상태 변경을 위한 전용 인커밍 포트
 */
public interface MemberDutyForDutyUseCase {

    void saveAllByDutyId(Long dutyId, List<Long> memberIds);

    void deleteAllByDutyId(Long dutyId);
}
