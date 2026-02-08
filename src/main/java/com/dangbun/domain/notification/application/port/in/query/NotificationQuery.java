package com.dangbun.domain.notification.application.port.in.query;

import com.dangbun.domain.notification.adapter.in.web.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetNotificationInfoResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetNotificationListResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetRecentSearchResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationQuery {

    GetMemberSearchListResponse searchMembers(String searchName, Pageable pageable);

    GetRecentSearchResponse getRecentSearches();

    GetNotificationListResponse getNotificationList(Pageable pageable);

    GetNotificationInfoResponse getNotificationInfo(Long notificationId);
}
