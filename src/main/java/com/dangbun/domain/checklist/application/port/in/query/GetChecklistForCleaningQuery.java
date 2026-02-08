package com.dangbun.domain.checklist.application.port.in.query;

import java.util.List;

/**
 * Cleaning 도메인에서 Checklist 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetChecklistForCleaningQuery {

    List<Long> findChecklistIdsByCleaningId(Long cleaningId);
}
