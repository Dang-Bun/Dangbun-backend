package com.dangbun.domain.place.application.port.out;

import com.dangbun.domain.place.domain.Place;

import java.util.Optional;

public interface PlaceQueryPort {
    Optional<Place> findById(Long placeId);

    Optional<Place> findByInviteCode(String inviteCode);
}
