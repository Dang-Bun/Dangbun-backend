package com.dangbun.domain.checklist.application.port.in.command;

import java.time.LocalTime;

/**
 * Calendar 도메인에서 Checklist 상태 변경을 위한 전용 인커밍 포트
 */
public interface ChecklistForCalendarUseCase {

    CompleteResult completeChecklist(Long checklistId, Long memberId);

    void deleteChecklist(Long checklistId);

    record CompleteResult(LocalTime completeTime) {}
}
