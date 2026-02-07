package com.dangbun.domain.notification.application.port.in.command;

import com.dangbun.domain.notification.adapter.in.web.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.adapter.in.web.dto.response.PostNotificationCreateResponse;

public interface NotificationCommandUseCase {

    PostNotificationCreateResponse createNotification(PostNotificationCreateRequest request);
}
