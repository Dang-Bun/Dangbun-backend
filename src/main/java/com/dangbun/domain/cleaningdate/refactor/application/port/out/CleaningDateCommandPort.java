package com.dangbun.domain.cleaningdate.refactor.application.port.out;

import com.dangbun.domain.cleaningdate.refactor.domain.CleaningDate;

import java.util.List;

public interface CleaningDateCommandPort {
    void saveAll(List<CleaningDate> cleaningDates);

    void deleteAllByCleaningId(Long cleaningId);
}
