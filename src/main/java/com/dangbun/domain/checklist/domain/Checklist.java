package com.dangbun.domain.checklist.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Checklist {

    private final ChecklistId checklistId;
    private final Long cleaningId;
    private final Boolean isComplete;
    private final Long completeMemberId;
    private final LocalDateTime completeTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Checklist withoutId(Long cleaningId) {
        return new Checklist(
                null,
                cleaningId,
                false,
                null,
                null,
                null,
                null
        );
    }

    public static Checklist withId(Long checklistId, Long cleaningId, Boolean isComplete,
                                   Long completeMemberId, LocalDateTime completeTime,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Checklist(
                new ChecklistId(checklistId),
                cleaningId,
                isComplete,
                completeMemberId,
                completeTime,
                createdAt,
                updatedAt
        );
    }

    public Checklist complete(Long memberId) {
        return new Checklist(
                this.checklistId,
                this.cleaningId,
                true,
                memberId,
                LocalDateTime.now(),
                this.createdAt,
                this.updatedAt
        );
    }

    public Checklist incomplete() {
        return new Checklist(
                this.checklistId,
                this.cleaningId,
                false,
                null,
                null,
                this.createdAt,
                this.updatedAt
        );
    }

    public record ChecklistId(Long value) {
    }
}
