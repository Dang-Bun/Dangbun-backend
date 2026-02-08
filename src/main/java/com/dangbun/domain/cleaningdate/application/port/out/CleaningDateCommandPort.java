package com.dangbun.domain.cleaningdate.application.port.out;

import com.dangbun.domain.cleaningdate.domain.CleaningDate;

import java.util.List;

public interface CleaningDateCommandPort {
    void saveAll(List<CleaningDate> cleaningDates);

    void deleteAllByCleaningId(Long cleaningId);
}
