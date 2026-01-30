package com.dangbun.domain.place.refactor.application.port.in.query;

import com.dangbun.domain.place.refactor.domain.Information;

public interface PlaceQuery {

    PlaceListResult getPlaceList(Long userId);

    Information checkInviteCode(Long userId, String invitedCode);

    PlaceResult getPlace();

    DutyProgressResult getDutiesProgress();

    PlaceTimeResult getTimeAndIsToday();

    String getInviteCode();
}
