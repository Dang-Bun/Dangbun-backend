package com.dangbun.domain.notification.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.notification.application.port.out.NotificationCommandPort;
import com.dangbun.domain.notification.application.port.out.NotificationQueryPort;
import com.dangbun.domain.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
class NotificationPersistenceAdapter implements NotificationQueryPort, NotificationCommandPort {

    private final SpringDataNotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Optional<Notification> findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notificationMapper::mapToDomainEntity);
    }

    @Override
    public Page<Notification> findBySenderId(Long senderId, Pageable pageable) {
        return notificationRepository.findBySender_MemberId(senderId, pageable)
                .map(notificationMapper::mapToDomainEntity);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationMapper.mapToJpaEntity(notification);
        NotificationJpaEntity saved = notificationRepository.save(entity);
        return notificationMapper.mapToDomainEntity(saved);
    }

    @Override
    public void deleteById(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
