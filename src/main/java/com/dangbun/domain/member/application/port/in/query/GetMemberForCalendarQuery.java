package com.dangbun.domain.member.application.port.in.query;

import java.util.Optional;

/**
 * Calendar 도메인에서 Member 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMemberForCalendarQuery {

    Optional<String> findMemberNameById(Long memberId);
}
