package com.dangbun.domain.notification.service;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.notification.adapter.in.web.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.adapter.in.web.dto.response.*;
import com.dangbun.domain.notification.adapter.out.persistence.NotificationJpaEntity;
import com.dangbun.domain.notification.domain.NotificationTemplate;
import com.dangbun.domain.notification.exception.custom.MemberNotFoundException;
import com.dangbun.domain.notification.exception.custom.NotificationAccessForbiddenException;
import com.dangbun.domain.notification.exception.custom.NotificationNotFoundException;
import com.dangbun.domain.notification.adapter.out.persistence.SpringDataNotificationRepository;
import com.dangbun.domain.notification.original.service.NotificationService;
import com.dangbun.domain.notificationreceiver.adapter.out.persistence.NotificationReceiverJpaEntity;
import com.dangbun.domain.notificationreceiver.adapter.out.persistence.SpringDataNotificationReceiverRepository;
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.redis.RedisService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationJpaEntityServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private SpringDataNotificationRepository springDataNotificationRepository;
    @Mock
    private SpringDataNotificationReceiverRepository notificationReceiverRepository;

    private MemberJpaEntity mockMember;
    private MemberJpaEntity mockReceiverMember;
    private final Long MOCK_PLACE_ID = 1L;
    private final Long MOCK_MEMBER_ID = 10L;
    private final Long MOCK_RECEIVER_ID = 20L;
    private final Long MOCK_NOTIFICATION_ID = 100L;

    @BeforeEach
    void setUp() {
        Place mockPlace = Place.builder().name("테스트 장소").category(CAFE).build();
        ReflectionTestUtils.setField(mockPlace, "placeId", MOCK_PLACE_ID);

        mockMember = MemberJpaEntity.builder().name("철수").build();
        ReflectionTestUtils.setField(mockMember, "memberId", MOCK_MEMBER_ID);
        ReflectionTestUtils.setField(mockMember, "place", mockPlace);

        mockReceiverMember = MemberJpaEntity.builder().name("영희").build();
        ReflectionTestUtils.setField(mockReceiverMember, "memberId", MOCK_RECEIVER_ID);
        ReflectionTestUtils.setField(mockReceiverMember, "place", mockPlace);

        MemberContext.set(mockMember);
    }


    @Test
    @DisplayName("수신인 멤버 검색 결과 조회 - 검색어 없는 경우 전체 멤버 목록 반환")
    void searchMembers_noSearchName() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberJpaEntity> memberPage = new PageImpl<>(List.of(mockMember, mockReceiverMember), pageable, 2);
        given(memberRepository.findByPlace_PlaceId(eq(MOCK_PLACE_ID), any(Pageable.class)))
                .willReturn(memberPage);

        // when
        GetMemberSearchListResponse result = notificationService.searchMembers(null, pageable);

        // then
        assertThat(result.members()).hasSize(2);
        assertThat(result.members().get(0).memberId()).isEqualTo(MOCK_MEMBER_ID);
        assertThat(result.members().get(1).memberId()).isEqualTo(MOCK_RECEIVER_ID);
        assertThat(result.hasNext()).isFalse();
        then(memberRepository).should().findByPlace_PlaceId(eq(MOCK_PLACE_ID), any(Pageable.class));
        then(redisService).should(never()).addRecentSearch(anyString(), anyString(), any(Integer.class));
    }

    @Test
    @DisplayName("수신인 멤버 검색 결과 조회 - 검색어 있는 경우, 최근 검색어에 추가하고 검색 결과 반환")
    void searchMembers_withSearchName() {
        // given
        String searchName = "영희";
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberJpaEntity> memberPage = new PageImpl<>(List.of(mockReceiverMember), pageable, 1);
        given(memberRepository.findByPlace_PlaceIdAndNameContaining(eq(MOCK_PLACE_ID), eq(searchName), any(Pageable.class)))
                .willReturn(memberPage);
        given(redisService.getRedisKey(anyLong(), anyLong())).willReturn("key_1_10");

        // when
        GetMemberSearchListResponse result = notificationService.searchMembers(searchName, pageable);

        // then
        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).memberId()).isEqualTo(MOCK_RECEIVER_ID);
        then(redisService).should().addRecentSearch("key_1_10", searchName, 5);
        then(memberRepository).should().findByPlace_PlaceIdAndNameContaining(eq(MOCK_PLACE_ID), eq(searchName), any(Pageable.class));
    }

    @Test
    @DisplayName("수신인 멤버 최근 검색어 조회 - 성공")
    void getRecentSearches_success() {
        // given
        List<String> recentSearches = List.of("철수", "영희");
        given(redisService.getRedisKey(anyLong(), anyLong())).willReturn("key_1_10");
        given(redisService.getRecentSearches(eq("key_1_10"), eq(5))).willReturn(recentSearches);

        // when
        GetRecentSearchResponse result = notificationService.getRecentSearches();

        // then
        assertThat(result.recentSearch()).isEqualTo(recentSearches);
        then(redisService).should().getRecentSearches("key_1_10", 5);
    }

    @Test
    @DisplayName("알림 작성 - 템플릿 사용 성공")
    void createNotification_template_success() {
        // given
        PostNotificationCreateRequest request = new PostNotificationCreateRequest(
                List.of(MOCK_RECEIVER_ID), CLEANING_PENDING, null
        );
        given(memberRepository.findAllById(anyList())).willReturn(List.of(mockReceiverMember));
        given(springDataNotificationRepository.save(any(NotificationJpaEntity.class))).willAnswer(invocation -> {
            NotificationJpaEntity savedNotificationJpaEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedNotificationJpaEntity, "notificationId", MOCK_NOTIFICATION_ID);
            return savedNotificationJpaEntity;
        });

        // when
        PostNotificationCreateResponse response = notificationService.createNotification(request);

        // then
        assertThat(response.notificationId()).isEqualTo(MOCK_NOTIFICATION_ID);
        then(memberRepository).should().findAllById(List.of(MOCK_RECEIVER_ID));
        then(springDataNotificationRepository).should().save(any(NotificationJpaEntity.class));
        then(notificationReceiverRepository).should(times(1)).save(any(NotificationReceiverJpaEntity.class));
    }

    @Test
    @DisplayName("알림 작성 - 직접 입력 성공")
    void createNotification_directContent_success() {
        // given
        PostNotificationCreateRequest request = new PostNotificationCreateRequest(
                List.of(MOCK_RECEIVER_ID), NONE, "안녕하세요. 많이 더우시죠? 그래도 화이팅입니다."
        );
        given(memberRepository.findAllById(anyList())).willReturn(List.of(mockReceiverMember));
        given(springDataNotificationRepository.save(any(NotificationJpaEntity.class))).willAnswer(invocation -> {
            NotificationJpaEntity savedNotificationJpaEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedNotificationJpaEntity, "notificationId", MOCK_NOTIFICATION_ID);
            return savedNotificationJpaEntity;
        });

        // when
        PostNotificationCreateResponse response = notificationService.createNotification(request);

        // then
        assertThat(response.notificationId()).isEqualTo(MOCK_NOTIFICATION_ID);
        then(memberRepository).should().findAllById(List.of(MOCK_RECEIVER_ID));
        then(springDataNotificationRepository).should().save(any(NotificationJpaEntity.class));
        then(notificationReceiverRepository).should(times(1)).save(any(NotificationReceiverJpaEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 수신자 중 존재하지 않는 멤버 ID가 있을 때 예외 발생")
    void createNotification_withNonExistentMemberId() {
        // given
        PostNotificationCreateRequest request = new PostNotificationCreateRequest(
                List.of(MOCK_RECEIVER_ID, 999L), NotificationTemplate.NONE, "테스트"
        );

        given(memberRepository.findAllById(anyList())).willReturn(List.of(mockReceiverMember));

        // when & then
        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(MemberNotFoundException.class);
        then(springDataNotificationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("알림 생성 - 수신자 중 해당 플레이스에 없는 멤버가 있을 때 예외 발생")
    void createNotification_memberNotFound() {
        // given
        MemberJpaEntity outsider = MemberJpaEntity.builder().name("외부인").build();
        ReflectionTestUtils.setField(outsider, "memberId", MOCK_RECEIVER_ID);

        Place outsidePlace = mock(Place.class);
        ReflectionTestUtils.setField(outsidePlace, "placeId", 999L);
        ReflectionTestUtils.setField(outsider, "place", outsidePlace);

        PostNotificationCreateRequest request = new PostNotificationCreateRequest(
                List.of(MOCK_RECEIVER_ID), NONE, "테스트"
        );

        given(memberRepository.findAllById(anyList())).willReturn(List.of(outsider));

        // when & then
        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(MemberNotFoundException.class);
        then(springDataNotificationRepository).should(never()).save(any());
        then(notificationReceiverRepository).should(never()).save(any());
    }


    @Test
    @DisplayName("알림 제목 추출 - 첫 문장이 25자 초과 시 줄임표(...)가 붙고 25자로 잘림")
    void extractTitle_withLongContent() {
        // given
        String longContent = "안녕하세요 오늘 복도 청소가 완료되었습니다 정말 수고가 많으십니다.";
        String expectedTitle = "안녕하세요 오늘 복도 청소가 완료되었습니다 정...";

        // when
        String actualTitle =  ReflectionTestUtils.invokeMethod(
                notificationService, "extractTitle", longContent);

        // then
        assertThat(actualTitle).isEqualTo(expectedTitle);
    }

    @Test
    @DisplayName("알림 제목 추출 - 제목으로 내용의 첫문장을 반환")
    void extractTitle_withShortContent(){
        // given
        String shortContent = "오늘은 대청소 하는 날! 다들 화이팅하세요.";
        String expectedTitle = "오늘은 대청소 하는 날!";

        // when
        String actualTitle =  ReflectionTestUtils.invokeMethod(
                notificationService, "extractTitle", shortContent);

        // then
        assertThat(actualTitle).isEqualTo(expectedTitle);
    }

    @Test
    @DisplayName("보낸 알림 목록 조회 - 성공")
    void getNotificationList_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        NotificationJpaEntity notificationJpaEntity1 = NotificationJpaEntity.builder().title("제목1").content("내용1").sender(mockMember).build();
        ReflectionTestUtils.setField(notificationJpaEntity1, "notificationId", 1L);
        ReflectionTestUtils.setField(notificationJpaEntity1, "createdAt", LocalDateTime.now());

        NotificationJpaEntity notificationJpaEntity2 = NotificationJpaEntity.builder().title("제목2").content("내용2").sender(mockMember).build();
        ReflectionTestUtils.setField(notificationJpaEntity2, "notificationId", 2L);
        ReflectionTestUtils.setField(notificationJpaEntity2, "createdAt", LocalDateTime.now());

        Page<NotificationJpaEntity> notificationPage = new PageImpl<>(List.of(notificationJpaEntity1, notificationJpaEntity2), pageable, 2);
        given(springDataNotificationRepository.findBySender_MemberId(eq(MOCK_MEMBER_ID), any(Pageable.class)))
                .willReturn(notificationPage);

        // when
        GetNotificationListResponse result = notificationService.getNotificationList(pageable);

        // then
        assertThat(result.notifications()).hasSize(2);
        assertThat(result.notifications().get(0).notificationId()).isEqualTo(1L);
        assertThat(result.hasNext()).isFalse();
        then(springDataNotificationRepository).should().findBySender_MemberId(eq(MOCK_MEMBER_ID), any(Pageable.class));
    }


    @Test
    @DisplayName("알림 상세 조회 (송신자) - 성공")
    void getNotificationInfo_asSender_success() {
        // given
        NotificationJpaEntity notificationJpaEntity = NotificationJpaEntity.builder().title("제목").content("내용").sender(mockMember).build();
        given(springDataNotificationRepository.findById(anyLong())).willReturn(Optional.of(notificationJpaEntity));
        given(notificationReceiverRepository.findAllByNotificationJpaEntity(any(NotificationJpaEntity.class)))
                .willReturn(List.of(
                        NotificationReceiverJpaEntity.builder().receiver(mockReceiverMember).notificationJpaEntity(notificationJpaEntity).build()
                ));

        // when
        GetNotificationInfoResponse result = notificationService.getNotificationInfo(MOCK_NOTIFICATION_ID);

        // then
        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.receiverNames()).containsExactly("영희");
        then(springDataNotificationRepository).should().findById(MOCK_NOTIFICATION_ID);
        then(notificationReceiverRepository).should(never()).existsByNotificationJpaEntityAndReceiver(any(), any());
        then(notificationReceiverRepository).should().findAllByNotificationJpaEntity(any());
    }

    @Test
    @DisplayName("알림 상세 조회 (수신자) - 성공")
    void getNotificationInfo_asReceiver_success() {
        // given
        MemberJpaEntity senderMember = MemberJpaEntity.builder().name("보낸이").build();
        ReflectionTestUtils.setField(senderMember, "memberId", 500L);

        NotificationJpaEntity notificationJpaEntity = NotificationJpaEntity.builder().title("제목").content("내용").sender(senderMember).build();
        ReflectionTestUtils.setField(notificationJpaEntity, "notificationId", 2L);

        given(springDataNotificationRepository.findById(anyLong())).willReturn(Optional.of(notificationJpaEntity));
        given(notificationReceiverRepository.existsByNotificationJpaEntityAndReceiver(any(NotificationJpaEntity.class), any(MemberJpaEntity.class)))
                .willReturn(true);
        given(notificationReceiverRepository.findAllByNotificationJpaEntity(any(NotificationJpaEntity.class)))
                .willReturn(List.of(NotificationReceiverJpaEntity.builder().receiver(mockMember).notificationJpaEntity(notificationJpaEntity).build()));

        // when
        GetNotificationInfoResponse result = notificationService.getNotificationInfo(MOCK_NOTIFICATION_ID);

        // then
        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.receiverNames()).containsExactly("철수");
        then(springDataNotificationRepository).should().findById(MOCK_NOTIFICATION_ID);
        then(notificationReceiverRepository).should().existsByNotificationJpaEntityAndReceiver(any(), any());
        then(notificationReceiverRepository).should().findAllByNotificationJpaEntity(any());
    }

    @Test
    @DisplayName("알림 상세 조회 - 존재하지 않는 알림 예외 발생")
    void getNotificationInfo_notFound() {
        // given
        given(springDataNotificationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.getNotificationInfo(MOCK_NOTIFICATION_ID))
                .isInstanceOf(NotificationNotFoundException.class);
        then(springDataNotificationRepository).should().findById(MOCK_NOTIFICATION_ID);
    }

    @Test
    @DisplayName("알림 상세 조회 - 권한 없는 유저 예외 발생")
    void getNotificationInfo_forbidden() {
        // given
        MemberJpaEntity anotherMember = MemberJpaEntity.builder().name("다른사람").build();
        ReflectionTestUtils.setField(anotherMember, "memberId", 888L);

        NotificationJpaEntity notificationJpaEntity = NotificationJpaEntity.builder().sender(anotherMember).build();
        ReflectionTestUtils.setField(notificationJpaEntity, "notificationId", 3L);

        given(springDataNotificationRepository.findById(anyLong())).willReturn(Optional.of(notificationJpaEntity));
        given(notificationReceiverRepository.existsByNotificationJpaEntityAndReceiver(any(NotificationJpaEntity.class), any(MemberJpaEntity.class)))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> notificationService.getNotificationInfo(MOCK_NOTIFICATION_ID))
                .isInstanceOf(NotificationAccessForbiddenException.class);
        then(springDataNotificationRepository).should().findById(MOCK_NOTIFICATION_ID);
        then(notificationReceiverRepository).should().existsByNotificationJpaEntityAndReceiver(any(), any());
    }
}
