package com.dangbun.domain.duty.application.port.in.query;

import java.util.List;

public record AddCleaningsResult(List<Long> assignedCleaningIds) {

    public static AddCleaningsResult of(List<Long> assignedCleaningIds) {
        return new AddCleaningsResult(assignedCleaningIds);
    }
}
