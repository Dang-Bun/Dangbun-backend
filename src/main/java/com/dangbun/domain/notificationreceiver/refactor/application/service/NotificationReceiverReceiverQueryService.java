package com.dangbun.domain.notificationreceiver.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.notification.domain.Notification;
import com.dangbun.domain.notificationreceiver.refactor.adapter.in.web.dto.response.GetNotificationReceivedListResponse;
import com.dangbun.domain.notificationreceiver.refactor.adapter.in.web.dto.response.GetNotificationReceivedListResponse.NotificationReceiverDto;
import com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence.NotificationReceiverJpaEntity;
import com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence.SpringDataNotificationReceiverRepository;
import com.dangbun.domain.notificationreceiver.refactor.application.port.in.query.CheckNotificationReceiverQuery;
import com.dangbun.domain.notificationreceiver.refactor.application.port.in.query.GetNotificationReceiverQuery;
import com.dangbun.domain.notificationreceiver.refactor.application.port.in.query.NotificationReceiverQuery;
import com.dangbun.domain.notificationreceiver.refactor.application.port.out.NotificationReceiverQueryPort;
import com.dangbun.domain.notificationreceiver.refactor.domain.NotificationReceiver;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReceiverReceiverQueryService implements NotificationReceiverQuery, CheckNotificationReceiverQuery, GetNotificationReceiverQuery {

    private final NotificationReceiverQueryPort notificationReceiverQueryPort;

    /*
     * TODO: JpaEntity 직접 사용 (DTO 변환에 연관 엔티티 정보 필요)
     * 도메인 모델 확장 또는 조회 전용 DTO 사용 검토 필요
     */
    private final SpringDataNotificationReceiverRepository notificationReceiverRepository;

    @Override
    public GetNotificationReceivedListResponse getReceivedNotifications(Pageable pageable) {
        Long receiverId = MemberContext.get().getMemberId();

        Page<NotificationReceiverJpaEntity> resultPage = notificationReceiverRepository.findByReceiver_MemberId(receiverId, pageable);

        List<NotificationReceiverDto> notifications = resultPage.getContent().stream()
                .map(NotificationReceiverDto::of)
                .toList();

        return GetNotificationReceivedListResponse.of(notifications, resultPage.hasNext());
    }

    @Override
    public int getUnreadCount() {
        Long memberId = MemberContext.get().getMemberId();
        return notificationReceiverQueryPort.countUnreadByMemberId(memberId);
    }

    @Override
    public boolean existsByNotificationJpaEntityAndReceiver(Notification notification, Member member) {
        return notificationReceiverQueryPort.existsByNotificationIdAndReceiverId(notification.getNotificationId().value(), member.getMemberId());
    }

    @Override
    public List<NotificationReceiver> findAllByNotificationId(Long notificationId) {
        return notificationReceiverQueryPort.findAllByNotificationId(notificationId);
    }
}
