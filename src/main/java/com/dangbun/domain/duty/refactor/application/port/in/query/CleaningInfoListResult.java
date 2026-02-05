package com.dangbun.domain.duty.refactor.application.port.in.query;

import java.util.List;

public record CleaningInfoListResult(List<CleaningInfo> cleanings) {

    public record CleaningInfo(
            Long cleaningId,
            String cleaningName,
            List<String> displayedNames,
            int memberCount
    ) {
    }

    public static CleaningInfoListResult of(List<CleaningInfo> cleanings) {
        return new CleaningInfoListResult(cleanings);
    }
}
