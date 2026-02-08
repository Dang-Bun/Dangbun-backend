package com.dangbun.domain.checklist.refactor.application.port.in.query;

import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.GetImageUrlResponse;

public interface ChecklistQuery {

    GetImageUrlResponse getImageUrl(Long checklistId);
}
