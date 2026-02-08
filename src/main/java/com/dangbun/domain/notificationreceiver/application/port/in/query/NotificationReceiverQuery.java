package com.dangbun.domain.notificationreceiver.application.port.in.query;

import com.dangbun.domain.notificationreceiver.adapter.in.web.dto.response.GetNotificationReceivedListResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationReceiverQuery {

    GetNotificationReceivedListResponse getReceivedNotifications(Pageable pageable);

    int getUnreadCount();
}
