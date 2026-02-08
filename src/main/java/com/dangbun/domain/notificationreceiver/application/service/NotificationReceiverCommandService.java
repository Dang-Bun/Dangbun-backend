package com.dangbun.domain.notificationreceiver.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.notificationreceiver.application.port.in.command.NotificationReceiverCommandUseCase;
import com.dangbun.domain.notificationreceiver.application.port.out.NotificationReceiverCommandPort;
import com.dangbun.domain.notificationreceiver.application.port.out.NotificationReceiverQueryPort;
import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.exception.custom.NotificationReceiverNotFoundException;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.notificationreceiver.response.status.NotificationReceiverExceptionResponse.NOTIFICATION_RECEIVER_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional
public class NotificationReceiverCommandService implements NotificationReceiverCommandUseCase {

    private final NotificationReceiverCommandPort commandPort;
    private final NotificationReceiverCommandPort notificationReceiverCommandPort;
    private final NotificationReceiverQueryPort notificationReceiverQueryPort;

    @Override
    public void save(NotificationReceiver notificationReceiver) {
        commandPort.save(notificationReceiver);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long memberId = MemberContext.get().getMemberId();

        NotificationReceiver receiver = notificationReceiverQueryPort.findByNotificationIdAndReceiverId(notificationId, memberId)
                .orElseThrow(() -> new NotificationReceiverNotFoundException(NOTIFICATION_RECEIVER_NOT_FOUND));

        notificationReceiverCommandPort.markAsRead(notificationId, receiver.getReceiverId());
        receiver.markAsRead();
    }
}
