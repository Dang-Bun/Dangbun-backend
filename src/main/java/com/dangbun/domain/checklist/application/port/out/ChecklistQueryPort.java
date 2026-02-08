package com.dangbun.domain.checklist.application.port.out;

import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.cleaning.domain.Cleaning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChecklistQueryPort {

    Optional<Checklist> findById(Long checklistId);

    List<Checklist> findByCleaningId(Long cleaningId);

    List<Checklist> findAllByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId);

    List<Checklist> findByPlaceAndMonth(Long placeId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    boolean existsCompletedChecklistByDateAndCleaning(LocalDateTime start, LocalDateTime end, Long cleaningId);

    Optional<Checklist> findByChecklistAndMemberId(Long checklistId, Long memberId);

    Cleaning getCleaningJpaEntity(Long checklistId);

    boolean existsByCleaningIdAndCreatedAt(Long cleaningId, LocalDateTime createdAt);

    // Calendar 도메인용 조회 메서드
    List<ChecklistCalendarDto> findAllWithCleaningAndDutyByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId);

    List<ChecklistCalendarDto> findWithCleaningByPlaceAndMonth(Long placeId, LocalDateTime start, LocalDateTime end);

    Optional<ChecklistWithCleaningDto> findWithCleaningInfoById(Long checklistId);

    Optional<ChecklistWithCleaningAndDutyDto> findWithCleaningAndDutyInfoById(Long checklistId);

    record ChecklistCalendarDto(
            Long checklistId,
            Long cleaningId,
            String cleaningName,
            String dutyName,
            Boolean isComplete,
            Long completeMemberId,
            LocalDateTime completeTime,
            Boolean needPhoto,
            LocalDateTime createdAt
    ) {}

    record ChecklistWithCleaningDto(
            Long checklistId,
            Long cleaningId,
            Boolean needPhoto
    ) {}

    record ChecklistWithCleaningAndDutyDto(
            Long checklistId,
            Long cleaningId,
            String cleaningName,
            String dutyName,
            Boolean needPhoto,
            String repeatType,
            String repeatDays
    ) {}
}
