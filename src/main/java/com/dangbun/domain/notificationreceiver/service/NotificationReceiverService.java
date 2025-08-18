package com.dangbun.domain.notificationreceiver.service;

import com.dangbun.domain.notificationreceiver.exception.custom.NotificationReceiverNotFoundException;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.notificationreceiver.dto.response.*;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.dto.response.GetNotificationReceivedListResponse.NotificationReceiverDto;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static com.dangbun.domain.notificationreceiver.response.status.NotificationReceiverExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class NotificationReceiverService {
    private final NotificationReceiverRepository notificationReceiverRepository;

    public GetNotificationReceivedListResponse getReceivedNotifications(Pageable pageable) {
        Long receiverId = MemberContext.get().getMemberId();

        Page<NotificationReceiver> resultPage = notificationReceiverRepository.findByReceiver_MemberId(receiverId, pageable);

        List<NotificationReceiverDto> notifications = resultPage.getContent().stream()
                .map(NotificationReceiverDto::of)
                .toList();

        return new GetNotificationReceivedListResponse(notifications, resultPage.hasNext());

    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Long memberId = MemberContext.get().getMemberId();

        NotificationReceiver receiver = notificationReceiverRepository
                .findByNotification_NotificationIdAndReceiver_MemberId(notificationId, memberId)
                .orElseThrow(() -> new NotificationReceiverNotFoundException(NOTIFICATION_RECEIVER_NOT_FOUND));

        receiver.markAsRead();
    }
}
