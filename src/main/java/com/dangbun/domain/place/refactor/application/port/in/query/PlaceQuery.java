package com.dangbun.domain.place.refactor.application.port.in.query;

import com.dangbun.domain.place.refactor.domain.Information;
import com.dangbun.domain.place.refactor.domain.Place;

import java.util.List;

public interface PlaceQuery {


    List<Place> getPlaceList(Long userId);

    Information checkInviteCode(Long userId, String invitedCode);


    PlaceResult getPlace();

    DutyProgressResult getDutiesProgress();

    PlaceTimeResult getTimeAndIsToday();

    String getInviteCode();


}
