package com.dangbun.domain.checklist.application.port.in.query;

import com.dangbun.domain.checklist.adapter.in.web.dto.response.GetImageUrlResponse;

public interface ChecklistQuery {

    GetImageUrlResponse getImageUrl(Long checklistId);
}
