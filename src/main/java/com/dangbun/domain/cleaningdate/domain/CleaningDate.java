package com.dangbun.domain.cleaningdate.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class CleaningDate {

    private final CleaningDateId cleaningDateId;

    private final LocalDate date;

    private final Long cleaningId;

    public static CleaningDate withoutId(LocalDate date, Long cleaningId) {
        return new CleaningDate(null, date, cleaningId);
    }

    public static CleaningDate withId(CleaningDateId cleaningDateId, LocalDate date, Long cleaningId) {
        return new CleaningDate(cleaningDateId, date, cleaningId);
    }

    public record CleaningDateId(Long value) {
    }
}
