package com.dangbun.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {
    CLEANING_PENDING("미완료된 청소를 진행해주세요."),
    NEW_MEMBER_JOINED("새로운 멤버가 참여했어요."),
    CLEANING_LIST_CHANGED("청소 목록 변동사항 발생했습니다."),
    NONE("직접 입력");

    private final String message;
}