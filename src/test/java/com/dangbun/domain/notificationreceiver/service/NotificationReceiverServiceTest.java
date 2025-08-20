package com.dangbun.domain.notificationreceiver.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.notification.entity.Notification;
import com.dangbun.domain.notificationreceiver.dto.response.GetNotificationReceivedListResponse;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiverId;
import com.dangbun.domain.notificationreceiver.exception.custom.NotificationReceiverNotFoundException;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class NotificationReceiverServiceTest {

    @InjectMocks
    private NotificationReceiverService notificationReceiverService;

    @Mock
    private NotificationReceiverRepository notificationReceiverRepository;

    private Member mockMember;
    private final Long MOCK_MEMBER_ID = 100L;

    @BeforeEach
    void setUp() {
        mockMember = Member.builder()
                .name("테스트멤버")
                .build();
        ReflectionTestUtils.setField(mockMember, "memberId", MOCK_MEMBER_ID);

        MemberContext.set(mockMember);
    }

    @Test
    @DisplayName("받은 알림 리스트 조회 성공")
    void getReceivedNotifications_success() {
        // given
        Notification notification = Notification.builder()
                .content("알림 내용")
                .sender(mockMember)
                .build();
        ReflectionTestUtils.setField(notification, "notificationId", 10L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());

        NotificationReceiver receiver = NotificationReceiver.builder()
                .notification(notification)
                .receiver(mockMember)
                .isRead(false)
                .build();
        ReflectionTestUtils.setField(receiver, "createdAt", LocalDateTime.now());
        NotificationReceiverId id = new NotificationReceiverId(200L, mockMember.getMemberId());
        ReflectionTestUtils.setField(receiver, "id", id);

        PageRequest pageable = PageRequest.of(0, 5);
        Page<NotificationReceiver> mockPage = new PageImpl<>(List.of(receiver), pageable, 1);

        given(notificationReceiverRepository.findByReceiver_MemberId(mockMember.getMemberId(), pageable))
                .willReturn(mockPage);

        // when
        GetNotificationReceivedListResponse response = notificationReceiverService.getReceivedNotifications(pageable);

        // then
        assertThat(response.notifications()).hasSize(1);
        assertThat(response.notifications().get(0).notificationReceiverId())
                .isEqualTo(new NotificationReceiverId(200L, mockMember.getMemberId()));
        assertThat(response.hasNext()).isFalse();

        then(notificationReceiverRepository)
                .should()
                .findByReceiver_MemberId(mockMember.getMemberId(), pageable);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsRead_success() {
        // given
        Notification notification = Notification.builder()
                .notificationId(20L)
                .content("테스트 알림")
                .build();

        NotificationReceiver receiver = spy(NotificationReceiver.builder()
                .notification(notification)
                .receiver(mockMember)
                .isRead(false)
                .build());

        given(notificationReceiverRepository.findByNotification_NotificationIdAndReceiver_MemberId(
                20L, mockMember.getMemberId()))
                .willReturn(Optional.of(receiver));

        // when
        notificationReceiverService.markAsRead(20L);

        // then
        then(receiver).should().markAsRead();
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 해당 알림 없음")
    void markAsRead_fail_notFound() {
        // given
        given(notificationReceiverRepository.findByNotification_NotificationIdAndReceiver_MemberId(
                20L, mockMember.getMemberId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationReceiverService.markAsRead(20L))
                .isInstanceOf(NotificationReceiverNotFoundException.class);
    }
}