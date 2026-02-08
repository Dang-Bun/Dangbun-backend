package com.dangbun.domain.place.application.port.service;

import com.dangbun.domain.checklist.application.port.in.query.GetCompletedChecklistQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningsByPlaceQuery;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.GetMembersByUserIdQuery;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.in.query.GetCleaningInfoByMemberQuery;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.notificationreceiver.application.port.in.query.GetUnreadNotificationCountQuery;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.application.port.out.PlaceQueryPort;
import com.dangbun.domain.place.domain.Information;
import com.dangbun.domain.place.domain.Place;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.domain.place.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.exception.custom.InviteCodeNotExistsException;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlaceQueryServiceTest {

    @InjectMocks
    private PlaceQueryService placeQueryService;

    @Mock
    private PlaceQueryPort placeQueryPort;

    @Mock
    private DutyQueryPort dutyQueryPort;

    @Mock
    private MemberDutyQueryPort memberDutyQueryPort;

    @Mock
    private GetMembersByUserIdQuery getMembersByUserIdQuery;

    @Mock
    private GetCleaningInfoByMemberQuery getCleaningInfoByMemberQuery;

    @Mock
    private GetCompletedChecklistQuery getCompletedChecklistQuery;

    @Mock
    private GetUnreadNotificationCountQuery getUnreadNotificationCountQuery;

    @Mock
    private GetCleaningsByPlaceQuery getCleaningsByPlaceQuery;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;

    @BeforeEach
    void setUp() {
        mockPlace = PlaceJpaEntity.builder()
                .name("테스트 카페")
                .build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 1L);
        ReflectionTestUtils.setField(mockPlace, "startTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(mockPlace, "endTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(mockPlace, "isToday", true);
        ReflectionTestUtils.setField(mockPlace, "inviteCode", "abc123");

        mockContextMember = MemberJpaEntity.builder()
                .name("매니저")
                .place(mockPlace)
                .role(MemberRole.MANAGER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);

        MemberContext.set(mockContextMember);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Test
    void 플레이스_목록_조회_대기중_멤버() {
        // given
        Long userId = 1L;
        Member waitingMember = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.WAITING,
                "대기 멤버",
                false,
                Map.of(),
                10L,
                userId,
                null
        );

        Place place = new Place(
                new Place.PlaceId(10L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(getMembersByUserIdQuery.getMembersByUserId(userId)).willReturn(List.of(waitingMember));
        given(placeQueryPort.findById(10L)).willReturn(Optional.of(place));

        // when
        PlaceListResult result = placeQueryService.getPlaceList(userId);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.placeId()).isEqualTo(10L);
        assertThat(placeDto.name()).isEqualTo("테스트 카페");
        assertThat(placeDto.totalCleaning()).isNull();
        assertThat(placeDto.role()).isNull();
    }

    @Test
    void 플레이스_목록_조회_일반_멤버() {
        // given
        Long userId = 1L;
        Member member = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "일반 멤버",
                true,
                Map.of(),
                10L,
                userId,
                null
        );

        Place place = new Place(
                new Place.PlaceId(10L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(getMembersByUserIdQuery.getMembersByUserId(userId)).willReturn(List.of(member));
        given(placeQueryPort.findById(10L)).willReturn(Optional.of(place));
        given(getCleaningInfoByMemberQuery.getCleaningIdsByMemberId(100L)).willReturn(List.of(1L, 2L));
        given(getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(any(), any(), eq(1L))).willReturn(true);
        given(getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(any(), any(), eq(2L))).willReturn(false);
        given(getUnreadNotificationCountQuery.getUnreadCountByMemberId(100L)).willReturn(3);

        // when
        PlaceListResult result = placeQueryService.getPlaceList(userId);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.totalCleaning()).isEqualTo(2);
        assertThat(placeDto.endCleaning()).isEqualTo(1);
        assertThat(placeDto.role()).isEqualTo("멤버");
        assertThat(placeDto.notifyNumber()).isEqualTo(3);
    }

    @Test
    void 플레이스_목록_조회_매니저() {
        // given
        Long userId = 1L;
        Member manager = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MANAGER,
                "매니저",
                true,
                Map.of(),
                10L,
                userId,
                null
        );

        Place place = new Place(
                new Place.PlaceId(10L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        Cleaning cleaning1 = Cleaning.withId(new Cleaning.CleaningId(1L), "청소1", 10L, null);
        Cleaning cleaning2 = Cleaning.withId(new Cleaning.CleaningId(2L), "청소2", 10L, null);

        given(getMembersByUserIdQuery.getMembersByUserId(userId)).willReturn(List.of(manager));
        given(placeQueryPort.findById(10L)).willReturn(Optional.of(place));
        given(getCleaningsByPlaceQuery.getCleaningsByPlaceId(10L)).willReturn(List.of(cleaning1, cleaning2));
        given(getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(any(), any(), eq(1L))).willReturn(true);
        given(getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(any(), any(), eq(2L))).willReturn(true);
        given(getUnreadNotificationCountQuery.getUnreadCountByMemberId(100L)).willReturn(0);

        // when
        PlaceListResult result = placeQueryService.getPlaceList(userId);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.totalCleaning()).isEqualTo(2);
        assertThat(placeDto.endCleaning()).isEqualTo(2);
        assertThat(placeDto.role()).isEqualTo("매니저");
    }

    @Test
    void 초대코드_확인_성공() {
        // given
        Long userId = 1L;
        String inviteCode = "abc123";

        Place place = new Place(
                new Place.PlaceId(10L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                inviteCode,
                null,
                null,
                null
        );

        Member existingMember = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MANAGER,
                "기존 멤버",
                true,
                Map.of("phone", "010-1234-5678"),
                10L,
                2L,
                null
        );

        given(placeQueryPort.findByInviteCode(inviteCode)).willReturn(Optional.of(place));
        given(getMembersByUserIdQuery.getMemberByUserIdAndPlaceId(userId, 10L)).willReturn(Optional.empty());
        given(getMembersByUserIdQuery.getFirstMemberByPlaceId(10L)).willReturn(Optional.of(existingMember));

        // when
        Information result = placeQueryService.checkInviteCode(userId, inviteCode);

        // then
        assertThat(result.getPlaceId()).isEqualTo(10L);
        assertThat(result.getInformation()).contains("phone");
    }

    @Test
    void 초대코드_확인_실패_잘못된_코드() {
        // given
        Long userId = 1L;
        String invalidCode = "wrong";

        given(placeQueryPort.findByInviteCode(invalidCode)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> placeQueryService.checkInviteCode(userId, invalidCode))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    void 초대코드_확인_실패_이미_가입됨() {
        // given
        Long userId = 1L;
        String inviteCode = "abc123";

        Place place = new Place(
                new Place.PlaceId(10L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                inviteCode,
                null,
                null,
                null
        );

        Member alreadyJoinedMember = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "이미 가입된 멤버",
                true,
                Map.of(),
                10L,
                userId,
                null
        );

        given(placeQueryPort.findByInviteCode(inviteCode)).willReturn(Optional.of(place));
        given(getMembersByUserIdQuery.getMemberByUserIdAndPlaceId(userId, 10L)).willReturn(Optional.of(alreadyJoinedMember));

        // when, then
        assertThatThrownBy(() -> placeQueryService.checkInviteCode(userId, inviteCode))
                .isInstanceOf(AlreadyInvitedException.class);
    }

    @Test
    void 시간_조회_성공() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));

        // when
        PlaceTimeResult result = placeQueryService.getTimeAndIsToday();

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.isToday()).isTrue();
    }

    @Test
    void 초대코드_조회_성공() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));

        // when
        String result = placeQueryService.getInviteCode();

        // then
        assertThat(result).isEqualTo("abc123");
    }

    @Test
    void 초대코드_조회_실패_코드없음() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                null,
                null,
                null,
                null
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));

        // when, then
        assertThatThrownBy(() -> placeQueryService.getInviteCode())
                .isInstanceOf(InviteCodeNotExistsException.class);
    }

    @Test
    void 플레이스_종료시간_조회_성공() {
        // given
        Long placeId = 1L;
        Place place = new Place(
                new Place.PlaceId(placeId),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );

        given(placeQueryPort.findById(placeId)).willReturn(Optional.of(place));

        // when
        LocalTime endTime = placeQueryService.getEndTimeByPlaceId(placeId);

        // then
        assertThat(endTime).isEqualTo(LocalTime.of(18, 0));
    }
}
