package com.dangbun.domain.cleaning.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum CleaningRepeatType {
    DAILY("매일"),
    WEEKLY("매주 요일마다"),
    MONTHLY_FIRST("매달 첫 날"),
    MONTHLY_LAST("매달 마지막 날"),
    NONE("없음")
    ;

    private final String displayName;
}
