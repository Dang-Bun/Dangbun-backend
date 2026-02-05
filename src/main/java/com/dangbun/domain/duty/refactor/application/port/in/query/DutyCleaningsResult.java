package com.dangbun.domain.duty.refactor.application.port.in.query;

import java.util.List;

public record DutyCleaningsResult(List<CleaningItem> cleanings) {

    public record CleaningItem(
            Long cleaningId,
            String name
    ) {
    }

    public static DutyCleaningsResult of(List<CleaningItem> cleanings) {
        return new DutyCleaningsResult(cleanings);
    }
}
