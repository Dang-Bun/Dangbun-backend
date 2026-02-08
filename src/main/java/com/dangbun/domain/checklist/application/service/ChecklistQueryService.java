package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.application.port.in.query.ChecklistQuery;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistQueryService implements ChecklistQuery {

    private final CleaningImageQuery cleaningImageQuery;

    @Override
    public GetImageUrlResponse getImageUrl(Long checklistId) {
        String accessUrl = cleaningImageQuery.getImageUrl(checklistId);
        return new GetImageUrlResponse(accessUrl);
    }
}
