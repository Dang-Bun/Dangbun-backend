package com.dangbun.domain.checklist.refactor.application.port.out;

import com.dangbun.domain.cleaningdate.domain.CleaningDate;

import java.util.List;

public interface ChecklistCommandPort {
    void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId);
}
