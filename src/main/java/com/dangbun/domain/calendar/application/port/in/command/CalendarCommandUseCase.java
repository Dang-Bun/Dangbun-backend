package com.dangbun.domain.calendar.application.port.in.command;

import com.dangbun.domain.calendar.adapter.in.web.dto.response.PatchUpdateChecklistToCompleteResponse;

public interface CalendarCommandUseCase {

    PatchUpdateChecklistToCompleteResponse finishChecklist(Long checklistId);

    void deleteChecklist(Long checklistId);
}
