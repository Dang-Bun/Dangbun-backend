package com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.notification.adapter.out.persistence.NotificationJpaEntity;
import com.dangbun.domain.notification.adapter.out.persistence.SpringDataNotificationRepository;
import com.dangbun.domain.notificationreceiver.refactor.application.port.out.NotificationReceiverCommandPort;
import com.dangbun.domain.notificationreceiver.refactor.application.port.out.NotificationReceiverQueryPort;
import com.dangbun.domain.notificationreceiver.refactor.domain.NotificationReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
class NotificationReceiverPersistenceAdapter implements NotificationReceiverQueryPort, NotificationReceiverCommandPort {

    private final SpringDataNotificationReceiverRepository notificationReceiverRepository;
    private final NotificationReceiverMapper notificationReceiverMapper;

    /*
     * TODO: Member/Notification 도메인 헥사고날 아키텍처 전환 완료 시 수정
     */
    private final MemberRepository memberRepository;
    private final SpringDataNotificationRepository notificationRepository;

    @Override
    public Page<NotificationReceiver> findByReceiverId(Long receiverId, Pageable pageable) {
        return notificationReceiverRepository.findByReceiver_MemberId(receiverId, pageable)
                .map(notificationReceiverMapper::mapToDomainEntity);
    }

    @Override
    public List<NotificationReceiver> findAllByNotificationId(Long notificationId) {
        NotificationJpaEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        return notificationReceiverRepository.findAllByNotificationJpaEntity(notification).stream()
                .map(notificationReceiverMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public Optional<NotificationReceiver> findByNotificationIdAndReceiverId(Long notificationId, Long receiverId) {
        return notificationReceiverRepository.findByNotificationJpaEntity_NotificationIdAndReceiver_MemberId(notificationId, receiverId)
                .map(notificationReceiverMapper::mapToDomainEntity);
    }

    @Override
    public boolean existsByNotificationIdAndReceiverId(Long notificationId, Long receiverId) {
        NotificationJpaEntity notification = notificationRepository.findById(notificationId).orElse(null);
        MemberJpaEntity member = memberRepository.findById(receiverId).orElse(null);

        if (notification == null || member == null) {
            return false;
        }

        return notificationReceiverRepository.existsByNotificationJpaEntityAndReceiver(notification, member);
    }

    @Override
    public int countUnreadByMemberId(Long memberId) {
        return notificationReceiverRepository.countUnreadByMemberId(memberId);
    }

    @Override
    public List<String> findReceiverNamesByNotificationId(Long notificationId) {
        NotificationJpaEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        return notificationReceiverRepository.findAllByNotificationJpaEntity(notification).stream()
                .map(nr -> nr.getReceiver().getName())
                .toList();
    }

    @Override
    public NotificationReceiver save(NotificationReceiver notificationReceiver) {
        NotificationReceiverJpaEntity entity = notificationReceiverMapper.mapToJpaEntity(notificationReceiver);
        NotificationReceiverJpaEntity saved = notificationReceiverRepository.save(entity);
        return notificationReceiverMapper.mapToDomainEntity(saved);
    }

    @Override
    public void markAsRead(Long notificationId, Long receiverId) {
        notificationReceiverRepository.findByNotificationJpaEntity_NotificationIdAndReceiver_MemberId(notificationId, receiverId)
                .ifPresent(NotificationReceiverJpaEntity::markAsRead);
    }

    @Override
    public void deleteByNotificationId(Long notificationId) {
        NotificationJpaEntity notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            List<NotificationReceiverJpaEntity> receivers = notificationReceiverRepository.findAllByNotificationJpaEntity(notification);
            notificationReceiverRepository.deleteAll(receivers);
        }
    }
}
