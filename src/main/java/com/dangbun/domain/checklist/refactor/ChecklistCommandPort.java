package com.dangbun.domain.checklist.refactor;

import com.dangbun.domain.cleaningdate.refactor.domain.CleaningDate;

import java.util.List;

public interface ChecklistCommandPort {
    public void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId);
}
