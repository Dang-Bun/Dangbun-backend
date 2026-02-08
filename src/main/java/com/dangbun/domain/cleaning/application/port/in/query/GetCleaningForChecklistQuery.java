package com.dangbun.domain.cleaning.application.port.in.query;

import com.dangbun.domain.cleaning.domain.CleaningRepeatType;

import java.util.List;
import java.util.Optional;

/**
 * Checklist 도메인에서 Cleaning 정보 조회를 위한 전용 인커밍 포트
 */
public interface GetCleaningForChecklistQuery {

    List<CleaningInfo> findAll();

    Optional<CleaningInfo> findById(Long cleaningId);

    record CleaningInfo(
            Long cleaningId,
            String name,
            CleaningRepeatType repeatType,
            String repeatDays,
            Boolean needPhoto,
            Long placeId
    ) {}
}
