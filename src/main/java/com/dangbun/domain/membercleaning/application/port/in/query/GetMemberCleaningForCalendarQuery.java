package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.List;

/**
 * Calendar 도메인에서 MemberCleaning 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMemberCleaningForCalendarQuery {

    List<String> findMemberNamesByCleaningId(Long cleaningId);

    List<Long> findCleaningIdsByMemberId(Long memberId);
}
