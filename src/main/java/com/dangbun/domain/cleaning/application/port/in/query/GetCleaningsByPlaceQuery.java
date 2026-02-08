package com.dangbun.domain.cleaning.application.port.in.query;

import com.dangbun.domain.cleaning.domain.Cleaning;

import java.util.List;

public interface GetCleaningsByPlaceQuery {

    List<Cleaning> getCleaningsByPlaceId(Long placeId);
}
