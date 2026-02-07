package com.dangbun.domain.notification.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.application.port.in.query.GetMemberPageQuery;
import com.dangbun.domain.member.application.port.in.query.GetMemberQuery;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetMemberSearchListResponse.MemberDto;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetNotificationInfoResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetNotificationListResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetNotificationListResponse.NotificationDto;
import com.dangbun.domain.notification.adapter.in.web.dto.response.GetRecentSearchResponse;
import com.dangbun.domain.notification.application.port.in.query.GetNotificationQuery;
import com.dangbun.domain.notification.application.port.in.query.NotificationQuery;
import com.dangbun.domain.notification.application.port.out.NotificationQueryPort;
import com.dangbun.domain.notification.domain.Notification;
import com.dangbun.domain.notification.exception.custom.NotificationAccessForbiddenException;
import com.dangbun.domain.notification.exception.custom.NotificationNotFoundException;
import com.dangbun.domain.notificationreceiver.application.port.in.query.CheckNotificationReceiverQuery;
import com.dangbun.domain.notificationreceiver.application.port.in.query.GetNotificationReceiverQuery;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.NOTIFICATION_ACCESS_FORBIDDEN;
import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.NOTIFICATION_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQuery, GetNotificationQuery {

    private final RedisService redisService;


    private static final int MAX_RECENT_COUNT = 5;

    private final NotificationQueryPort notificationQueryPort;

    private final GetMemberPageQuery getMemberPageQuery;
    private final GetMemberQuery getMemberQuery;
    private final CheckNotificationReceiverQuery checkNotificationReceiverQuery;
    private final GetNotificationReceiverQuery getNotificationReceiverQuery;

    @Override
    public GetMemberSearchListResponse searchMembers(String searchName, Pageable pageable) {
        MemberJpaEntity me = MemberContext.get();
        Long memberId = me.getMemberId();
        Long placeId = me.getPlace().getPlaceId();

        Page<Member> memberPage;
        if (searchName == null || searchName.isBlank()) {
            memberPage = getMemberPageQuery.getPagedMemberByPlaceId(placeId, pageable);
        } else {
            String redisKey = redisService.getRedisKey(placeId, memberId);
            redisService.addRecentSearch(redisKey, searchName, MAX_RECENT_COUNT);
            memberPage = getMemberPageQuery.getPageMemberByPlaceIdAndNameContaining(placeId, searchName, pageable);
        }

        List<MemberDto> memberDtos = memberPage.getContent().stream()
                .map(MemberDto::of)
                .toList();

        return GetMemberSearchListResponse.of(memberDtos, memberPage.hasNext());
    }

    @Override
    public GetRecentSearchResponse getRecentSearches() {
        MemberJpaEntity me = MemberContext.get();
        Long memberId = me.getMemberId();
        Long placeId = me.getPlace().getPlaceId();

        String redisKey = redisService.getRedisKey(placeId, memberId);
        List<String> recentSearches = redisService.getRecentSearches(redisKey, MAX_RECENT_COUNT);
        return GetRecentSearchResponse.of(recentSearches);
    }

    @Override
    public GetNotificationListResponse getNotificationList(Pageable pageable) {
        Long senderId = MemberContext.get().getMemberId();

        Page<Notification> resultPage = notificationQueryPort.findBySenderId(senderId, pageable);

        List<NotificationDto> notifications = resultPage.getContent().stream()
                .map(NotificationDto::of)
                .toList();

        return new GetNotificationListResponse(notifications, resultPage.hasNext());
    }

    @Override
    public GetNotificationInfoResponse getNotificationInfo(Long notificationId) {


        Member member = getMemberQuery.getMemberById(MemberContext.get().getMemberId());

        Notification notification = notificationQueryPort.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(NOTIFICATION_NOT_FOUND));

        boolean isSender = notification.getSenderId().equals(member.getMemberId());

        boolean isReceiver = false;
        if (!isSender) {
            isReceiver = checkNotificationReceiverQuery.existsByNotificationJpaEntityAndReceiver(notification, member);
        }


        if (!isSender && !isReceiver) {
            throw new NotificationAccessForbiddenException(NOTIFICATION_ACCESS_FORBIDDEN);
        }

        List<String> receiverNames = getNotificationReceiverQuery.findAllByNotificationId(notification.getNotificationId().value())
                .stream()
                .map(nr -> getMemberQuery.getMemberById(nr.getReceiverId()).getName())
                .toList();

        return GetNotificationInfoResponse.of(notification, receiverNames);
    }

    @Override
    public Notification getNotification(Long notificationId) {
        return notificationQueryPort.findById(notificationId).get();
    }
}
