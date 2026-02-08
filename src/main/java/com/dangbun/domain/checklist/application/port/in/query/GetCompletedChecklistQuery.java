package com.dangbun.domain.checklist.application.port.in.query;

import java.time.LocalDateTime;

public interface GetCompletedChecklistQuery {

    boolean existsCompletedChecklistByDateAndCleaningId(LocalDateTime start, LocalDateTime end, Long cleaningId);
}
