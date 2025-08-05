package com.dangbun.domain.notification.service;

import com.dangbun.domain.member.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.member.service.MemberService;
import com.dangbun.domain.notification.dto.request.*;
import com.dangbun.domain.notification.dto.response.*;
import com.dangbun.domain.notification.dto.response.GetNotificationListResponse.NotificationDto;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse.MemberDto;
import com.dangbun.domain.notification.entity.Notification;
import com.dangbun.domain.notification.entity.NotificationTemplate;
import com.dangbun.domain.notification.exception.custom.*;
import com.dangbun.domain.notification.repository.NotificationRepository;

import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.global.redis.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final RedisService redisService;
    private static final int MAX_RECENT_COUNT = 5;
    private final MemberService memberService;
    private final NotificationReceiverRepository notificationReceiverRepository;
    private final NotificationRepository notificationRepository;



    public GetMemberSearchListResponse searchMembers( String searchName, Pageable pageable) {
        Member me = MemberContext.get();
        Long memberId = me.getMemberId();
        Long placeId = me.getPlace().getPlaceId();

        Page<Member> memberPage;
        if (searchName == null || searchName.isBlank()) {
            memberPage = memberRepository.findByPlace_PlaceId(placeId, pageable);
        } else {
            String redisKey = redisService.getRedisKey(placeId, memberId);
            redisService.addRecentSearch(redisKey, searchName, MAX_RECENT_COUNT);
            memberPage = memberRepository.findByPlace_PlaceIdAndNameContaining(placeId, searchName, pageable);
        }

        List<MemberDto> memberDtos = memberPage.getContent().stream()
                .map(MemberDto::of)
                .toList();

        return GetMemberSearchListResponse.of(memberDtos, memberPage.hasNext());
    }

    public GetRecentSearchResponse getRecentSearches() {
        Member me = MemberContext.get();
        Long memberId = me.getMemberId();
        Long placeId = me.getPlace().getPlaceId();

        String redisKey = redisService.getRedisKey(placeId, memberId);
        List<String> recentSearches = redisService.getRecentSearches(redisKey, MAX_RECENT_COUNT);
        return GetRecentSearchResponse.of(recentSearches);
    }

    @Transactional
    public PostNotificationCreateResponse createNotification(PostNotificationCreateRequest request) {
        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> receiverMembers = memberRepository.findAllById(request.receiverMemberIds());
        boolean allBelongToPlace = receiverMembers.stream()
                .allMatch(m -> m.getPlace().getPlaceId().equals(placeId));

        if (!allBelongToPlace) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }

        String content = request.template() == NotificationTemplate.NONE
                ? request.content()
                : request.template().getMessage();

        String title = extractTitle(content);
        System.out.println(title);
        Notification notification = Notification.builder()
                .template(request.template())
                .title(title)
                .content(content)
                .sender(me)
                .build();

        notificationRepository.save(notification);

        for (Member receiverMember : receiverMembers) {
            NotificationReceiver receiver = NotificationReceiver.builder()
                    .notification(notification)
                    .receiver(receiverMember)
                    .isRead(false)
                    .build();
            notificationReceiverRepository.save(receiver);
        }

        return PostNotificationCreateResponse.of(notification.getNotificationId());
    }

    private String extractTitle(String content) {
        int minIdx = 1000;

        for (String delimiter : new String[]{".", "!", "?"}) {
            int idx = content.indexOf(delimiter);
            if (idx != -1 && idx < minIdx) {
                minIdx = idx;
            }
        }

        if (minIdx != 1000) {
            String sentence = content.substring(0, minIdx + 1);
            System.out.println(sentence);
            return sentence.length() <= 25 ? sentence : sentence.substring(0, 25) + "...";
        }

        return content.length() > 25 ? content.substring(0, 25) + "..." : content;
    }

    public GetNotificationListResponse getNotificationList(Pageable pageable) {
        Long senderId = MemberContext.get().getMemberId();

        Page<Notification> resultPage = notificationRepository.findBySender_MemberId(senderId, pageable);

        List<NotificationDto> notifications = resultPage.getContent().stream()
                .map(NotificationDto::of)
                .toList();

        return new GetNotificationListResponse(notifications, resultPage.hasNext());

    }

    public GetNotificationInfoResponse getNotificationInfo(Long notificationId) {
        Member member = MemberContext.get();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(NOTIFICATION_NOT_FOUND));

        boolean isSender = notification.getSender().getMemberId().equals(member.getMemberId());

        boolean isReceiver = notificationReceiverRepository.existsByNotificationAndReceiver(notification, member);

        if (!isSender && !isReceiver) {
            throw new NotificationAccessForbiddenException(NOTIFICATION_ACCESS_FORBIDDEN);
        }

        List<String> receiverNames = notificationReceiverRepository.findAllByNotification(notification)
                .stream()
                .map(nr -> nr.getReceiver().getName())
                .toList();

        return GetNotificationInfoResponse.of(notification, receiverNames);
    }
}

