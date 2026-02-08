package com.dangbun.domain.notification.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.application.port.in.query.GetAllMemberQuery;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.notification.adapter.in.web.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.adapter.in.web.dto.response.PostNotificationCreateResponse;
import com.dangbun.domain.notification.application.port.in.command.NotificationCommandUseCase;
import com.dangbun.domain.notification.application.port.out.NotificationCommandPort;
import com.dangbun.domain.notification.domain.Notification;
import com.dangbun.domain.notification.domain.NotificationTemplate;
import com.dangbun.domain.notification.exception.custom.MemberNotFoundException;
import com.dangbun.domain.notificationreceiver.application.port.in.command.NotificationReceiverCommandUseCase;
import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.MEMBER_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService implements NotificationCommandUseCase {

    private final GetAllMemberQuery memberQuery;
    private final NotificationReceiverCommandUseCase notificationReceiverCommandUseCase;
    private final NotificationCommandPort notificationCommandPort;


    @Override
    public PostNotificationCreateResponse createNotification(PostNotificationCreateRequest request) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        List<Member> receiverMembers = memberQuery.findAllByIds(request.receiverMemberIds());

        if (receiverMembers.size() != request.receiverMemberIds().size()) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }

        boolean allBelongToPlace = receiverMembers.stream()
                .allMatch(m -> m.getPlaceId().equals(placeId));

        if (!allBelongToPlace) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }

        String content = request.template() == NotificationTemplate.NONE
                ? request.content()
                : request.template().getMessage();

        String title = extractTitle(content);

        Notification notification = Notification.withoutId(
                request.template(),
                title,
                content,
                me.getMemberId(),
                me.getName()
        );

        Notification savedNotification = notificationCommandPort.save(notification);


        for (Member receiverMember : receiverMembers) {
            NotificationReceiver receiver = NotificationReceiver.of(
                    receiverMember.getMemberId(),
                    savedNotification.getNotificationId().value(),
                    false,
                    null);
            notificationReceiverCommandUseCase.save(receiver);
        }

        return PostNotificationCreateResponse.of(savedNotification.getNotificationId().value());
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
            return sentence.length() <= 25 ? sentence : sentence.substring(0, 25) + "...";
        }

        return content.length() > 25 ? content.substring(0, 25) + "..." : content;
    }
}
