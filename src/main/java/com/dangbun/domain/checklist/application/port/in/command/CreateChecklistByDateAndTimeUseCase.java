package com.dangbun.domain.checklist.application.port.in.command;

import com.dangbun.domain.cleaningdate.domain.CleaningDate;

import java.util.List;

public interface CreateChecklistByDateAndTimeUseCase {

    void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId);
}
