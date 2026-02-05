package com.dangbun.domain.cleaning.refactor.application.port.in.command;


import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.response.PostCleaningResponse;

public interface CleaningCommandUseCase {

    PostCleaningResponse createCleaning(PostCleaningCreateRequest request);

    void updateCleaning(Long cleaningId, PutCleaningUpdateRequest request);

    void deleteCleaning(Long cleaningId);
}
