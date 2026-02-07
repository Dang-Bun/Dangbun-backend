package com.dangbun.domain.notificationreceiver.refactor.application.port.in.query;

import com.dangbun.domain.notificationreceiver.refactor.adapter.in.web.dto.response.GetNotificationReceivedListResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationReceiverQuery {

    GetNotificationReceivedListResponse getReceivedNotifications(Pageable pageable);

    int getUnreadCount();
}
