package com.dangbun.domain.checklist.application.port.in.query;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Calendar 도메인에서 Checklist 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetChecklistForCalendarQuery {

    List<ChecklistCalendarInfo> findAllByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId);

    List<ChecklistCalendarInfo> findByPlaceAndMonth(Long placeId, LocalDateTime start, LocalDateTime end);

    Optional<ChecklistWithCleaningInfo> findWithCleaningById(Long checklistId);

    Optional<ChecklistWithCleaningAndDutyInfo> findWithCleaningAndDutyById(Long checklistId);

    record ChecklistCalendarInfo(
            Long checklistId,
            Long cleaningId,
            String cleaningName,
            String dutyName,
            Boolean isComplete,
            Long completeMemberId,
            LocalDateTime completeTime,
            Boolean needPhoto,
            LocalDateTime createdAt
    ) {
        public LocalTime getCompleteLocalTime() {
            return completeTime != null ? completeTime.toLocalTime() : null;
        }
    }

    record ChecklistWithCleaningInfo(
            Long checklistId,
            Long cleaningId,
            Boolean needPhoto
    ) {}

    record ChecklistWithCleaningAndDutyInfo(
            Long checklistId,
            Long cleaningId,
            String cleaningName,
            String dutyName,
            Boolean needPhoto,
            String repeatType,
            String repeatDays
    ) {}
}
