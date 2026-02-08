package com.dangbun.domain.checklist.application.port.in.command;

import java.time.LocalDateTime;

public interface GenerateDailyChecklistsUseCase {

    void generateDailyChecklists(LocalDateTime now);
}
