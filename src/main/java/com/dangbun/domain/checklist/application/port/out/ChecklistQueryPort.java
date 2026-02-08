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
}
