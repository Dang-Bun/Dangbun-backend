package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.application.port.in.query.ChecklistQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetCompletedChecklistQuery;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistQueryService implements ChecklistQuery, GetCompletedChecklistQuery {

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
}
