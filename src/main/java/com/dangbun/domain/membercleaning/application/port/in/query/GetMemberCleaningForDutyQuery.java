package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.List;

/**
 * Duty 도메인에서 MemberCleaning 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMemberCleaningForDutyQuery {

    List<String> findMemberNamesByCleaningId(Long cleaningId);

    Integer countMembersByCleaningId(Long cleaningId);
}
