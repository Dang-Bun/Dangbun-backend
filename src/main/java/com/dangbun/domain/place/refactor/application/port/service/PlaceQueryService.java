package com.dangbun.domain.place.refactor.application.port.service;

import com.dangbun.domain.place.refactor.application.port.in.query.DutyProgressResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceQuery;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.refactor.domain.Information;
import com.dangbun.domain.place.refactor.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceQueryService implements PlaceQuery {
    @Override
    public List<Place> getPlaceList(Long userId) {
        return List.of();
    }

    @Override
    public Information checkInviteCode(Long userId, String invitedCode) {
        return null;
    }

    @Override
    public PlaceResult getPlace() {
        return null;
    }

    @Override
    public DutyProgressResult getDutiesProgress() {
        return null;
    }

    @Override
    public PlaceTimeResult getTimeAndIsToday() {
        return null;
    }

    @Override
    public String getInviteCode() {
        return "";
    }
}
