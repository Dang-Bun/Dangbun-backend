package com.dangbun.domain.membercleaning.application.port.in.command;

import java.util.List;

/**
 * Cleaning 도메인에서 MemberCleaning 상태 변경을 위한 전용 인커밍 포트
 */
public interface MemberCleaningForCleaningUseCase {

    void saveAllByCleaningIdAndMemberIds(Long cleaningId, List<Long> memberIds);

    void deleteAllByCleaningId(Long cleaningId);
}
