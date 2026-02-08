package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.application.port.in.query.ChecklistQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCleaningQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetCompletedChecklistQuery;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistQueryService implements ChecklistQuery, GetCompletedChecklistQuery, GetChecklistForCalendarQuery, GetChecklistForCleaningQuery {

    private final CleaningImageQuery cleaningImageQuery;
    private final ChecklistQueryPort checklistQueryPort;

    @Override
    public GetImageUrlResponse getImageUrl(Long checklistId) {
        String accessUrl = cleaningImageQuery.getImageUrl(checklistId);
        return new GetImageUrlResponse(accessUrl);
    }

    @Override
    public boolean existsCompletedChecklistByDateAndCleaningId(LocalDateTime start, LocalDateTime end, Long cleaningId) {
        return checklistQueryPort.existsCompletedChecklistByDateAndCleaning(start, end, cleaningId);
    }

    // GetChecklistForCalendarQuery 구현
    @Override
    public List<ChecklistCalendarInfo> findAllByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId) {
        return checklistQueryPort.findAllWithCleaningAndDutyByCreatedDateAndPlaceId(start, end, placeId).stream()
                .map(dto -> new ChecklistCalendarInfo(
                        dto.checklistId(),
                        dto.cleaningId(),
                        dto.cleaningName(),
                        dto.dutyName(),
                        dto.isComplete(),
                        dto.completeMemberId(),
                        dto.completeTime(),
                        dto.needPhoto(),
                        dto.createdAt()
                ))
                .toList();
    }

    @Override
    public List<ChecklistCalendarInfo> findByPlaceAndMonth(Long placeId, LocalDateTime start, LocalDateTime end) {
        return checklistQueryPort.findWithCleaningByPlaceAndMonth(placeId, start, end).stream()
                .map(dto -> new ChecklistCalendarInfo(
                        dto.checklistId(),
                        dto.cleaningId(),
                        dto.cleaningName(),
                        dto.dutyName(),
                        dto.isComplete(),
                        dto.completeMemberId(),
                        dto.completeTime(),
                        dto.needPhoto(),
                        dto.createdAt()
                ))
                .toList();
    }

    @Override
    public Optional<ChecklistWithCleaningInfo> findWithCleaningById(Long checklistId) {
        return checklistQueryPort.findWithCleaningInfoById(checklistId)
                .map(dto -> new ChecklistWithCleaningInfo(
                        dto.checklistId(),
                        dto.cleaningId(),
                        dto.needPhoto()
                ));
    }

    @Override
    public Optional<ChecklistWithCleaningAndDutyInfo> findWithCleaningAndDutyById(Long checklistId) {
        return checklistQueryPort.findWithCleaningAndDutyInfoById(checklistId)
                .map(dto -> new ChecklistWithCleaningAndDutyInfo(
                        dto.checklistId(),
                        dto.cleaningId(),
                        dto.cleaningName(),
                        dto.dutyName(),
                        dto.needPhoto(),
                        dto.repeatType(),
                        dto.repeatDays()
                ));
    }

    // GetChecklistForCleaningQuery 구현
    @Override
    public List<Long> findChecklistIdsByCleaningId(Long cleaningId) {
        return checklistQueryPort.findByCleaningId(cleaningId).stream()
                .map(checklist -> checklist.getChecklistId().value())
                .toList();
    }
}
