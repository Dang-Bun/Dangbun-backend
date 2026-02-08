package com.dangbun.domain.duty.application.port.out;

import com.dangbun.domain.duty.domain.Duty;

import java.util.List;

public interface DutyCommandPort {
    Duty save(Duty duty);

    void delete(Duty duty);

    List<Duty> findByPlaceId(Long placeId);
}
