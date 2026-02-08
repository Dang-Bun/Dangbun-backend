package com.dangbun.domain.checklist.application.port.out;

import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;

import java.util.List;

public interface ChecklistCommandPort {
    void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId);

    Checklist completeChecklist(Long checklistId, Long memberId);

    Checklist incompleteChecklist(Long checklistId);

    void createChecklist(Long cleaningId);

    void deleteById(Long checklistId);
}
