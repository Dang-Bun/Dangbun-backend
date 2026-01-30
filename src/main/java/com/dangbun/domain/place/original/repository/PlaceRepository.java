package com.dangbun.domain.place.original.repository;

import com.dangbun.domain.place.original.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Place findByInviteCode(String code);
}
