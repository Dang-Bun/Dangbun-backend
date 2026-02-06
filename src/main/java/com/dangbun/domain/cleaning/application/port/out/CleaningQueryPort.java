package com.dangbun.domain.cleaning.application.port.out;

import com.dangbun.domain.cleaning.domain.Cleaning;

import java.util.List;
import java.util.Optional;

public interface CleaningQueryPort {

    Optional<Cleaning> findById(Long cleaningId);

    Optional<Cleaning> findWithDutyNullableById(Long cleaningId);

    Optional<Cleaning> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId);

    List<Cleaning> findAllByDutyId(Long dutyId);

    List<Cleaning> findByDutyIdAndMemberIds(Long dutyId, List<Long> memberIds);

    List<Cleaning> findUnassignedCleaningsByPlaceId(Long placeId);

    List<Cleaning> findByPlaceId(Long placeId);

    boolean existsByNameAndDutyIdAndPlaceId(String name, Long dutyId, Long placeId);

    boolean existsByNameAndDutyIdAndCleaningIdNotAndPlaceId(String name, Long dutyId, Long cleaningId, Long placeId);
}
