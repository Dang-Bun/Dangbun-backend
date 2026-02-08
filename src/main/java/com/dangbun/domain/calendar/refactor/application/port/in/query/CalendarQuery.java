package com.dangbun.domain.calendar.refactor.application.port.in.query;

import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetChecklistsResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetCleaningInfoResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetProgressBarsResponse;

import java.time.LocalDate;

public interface CalendarQuery {

    GetChecklistsResponse getChecklists(LocalDate date);

    GetProgressBarsResponse getProgressBars(int year, int month);

    GetImageUrlResponse getPhotoUrl(Long checklistId);

    GetCleaningInfoResponse getCleaningInfo(Long checklistId);
}
