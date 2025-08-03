package com.dangbun.domain.notificationreceiver.service;

import com.dangbun.domain.member.MemberContext;
import com.dangbun.domain.notificationreceiver.dto.response.*;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.dto.response.GetNotificationReceivedListResponse.NotificationReceiverDto;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
