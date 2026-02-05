package com.dangbun.domain.duty.refactor.application.port.out;

import com.dangbun.domain.duty.refactor.domain.Duty;

import java.util.List;

public interface DutyCommandPort {
    Duty save(Duty duty);

    void delete(Duty duty);

    List<Duty> findByPlaceId(Long placeId);
}
