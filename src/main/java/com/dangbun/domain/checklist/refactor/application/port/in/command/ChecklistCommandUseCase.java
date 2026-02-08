package com.dangbun.domain.checklist.refactor.application.port.in.command;

import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostIncompleteChecklistResponse;

public interface ChecklistCommandUseCase {

    PostCompleteChecklistResponse completeChecklist();

    PostIncompleteChecklistResponse incompleteChecklist();

    PostGetPresignedUrlResponse generateImageUrl(PostGetPresignedUrlRequest request);

    void saveUploadResult(PostSaveUploadResultRequest request);

    void deleteImage(Long checklistId);
}
