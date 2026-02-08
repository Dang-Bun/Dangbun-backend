package com.dangbun.domain.member.application.port.in.query;

import java.util.List;

/**
 * Duty 도메인에서 Member 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetMembersForDutyQuery {

    List<MemberInfo> findAllByIds(List<Long> memberIds);

    record MemberInfo(Long memberId, String name) {}
}
