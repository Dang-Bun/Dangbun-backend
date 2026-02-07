package com.dangbun.domain.notificationreceiver.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence.NotificationReceiverJpaEntity;
import com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence.SpringDataNotificationReceiverRepository;
import com.dangbun.domain.notificationreceiver.refactor.application.port.in.command.NotificationReceiverCommandUseCase;
import com.dangbun.domain.notificationreceiver.refactor.application.port.out.NotificationReceiverCommandPort;
import com.dangbun.domain.notificationreceiver.refactor.domain.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.refactor.exception.custom.NotificationReceiverNotFoundException;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.notificationreceiver.refactor.response.status.NotificationReceiverExceptionResponse.NOTIFICATION_RECEIVER_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional
public class NotificationReceiverCommandService implements NotificationReceiverCommandUseCase {

    /*
     * TODO: JpaEntity 직접 사용 (markAsRead 상태 변경에 영속성 컨텍스트 필요)
     * CommandPort를 통한 업데이트로 전환 검토 필요
     */
    private final SpringDataNotificationReceiverRepository notificationReceiverRepository;
    private final NotificationReceiverCommandPort commandPort;

    @Override
    public void save(NotificationReceiver notificationReceiver) {
        commandPort.save(notificationReceiver);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long memberId = MemberContext.get().getMemberId();

        NotificationReceiverJpaEntity receiver = notificationReceiverRepository
                .findByNotificationJpaEntity_NotificationIdAndReceiver_MemberId(notificationId, memberId)
                .orElseThrow(() -> new NotificationReceiverNotFoundException(NOTIFICATION_RECEIVER_NOT_FOUND));

        receiver.markAsRead();
    }
}
