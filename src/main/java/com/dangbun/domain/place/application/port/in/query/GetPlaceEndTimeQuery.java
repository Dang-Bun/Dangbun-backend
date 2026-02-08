package com.dangbun.domain.place.application.port.in.query;

import java.time.LocalTime;

public interface GetPlaceEndTimeQuery {

    LocalTime getEndTimeByPlaceId(Long placeId);
}
