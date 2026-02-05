package com.dangbun.domain.duty.refactor.application.port.out;

import com.dangbun.domain.duty.refactor.domain.Duty;

import java.util.List;
import java.util.Optional;

public interface DutyQueryPort {
    Optional<Duty> findById(Long dutyId);

    Optional<Duty> findByIdAndPlaceId(Long dutyId, Long placeId);

    List<Duty> findByPlaceId(Long placeId);

    boolean existsByNameAndPlaceId(String name, Long placeId);

    String getDutyNameById(Long dutyId);

    List<Duty> findAll();

    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);
}
