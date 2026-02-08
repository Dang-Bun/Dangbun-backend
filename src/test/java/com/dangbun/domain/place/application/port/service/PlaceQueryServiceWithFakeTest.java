package com.dangbun.domain.place.application.port.service;

import com.dangbun.domain.checklist.application.port.in.query.FakeChecklistForCalendarQuery;
import com.dangbun.domain.checklist.application.port.in.query.FakeCompletedChecklistQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery.ChecklistCalendarInfo;
import com.dangbun.domain.cleaning.application.port.in.query.FakeCleaningsByPlaceQuery;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.duty.adapter.out.persistence.FakeDutyRepository;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.FakeMembersByUserIdQuery;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.in.query.FakeCleaningInfoByMemberQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.FakeMembersByCleaningQuery;
import com.dangbun.domain.memberduty.adapter.out.persistence.FakeMemberDutyRepository;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import com.dangbun.domain.notificationreceiver.application.port.in.query.FakeUnreadNotificationCountQuery;
import com.dangbun.domain.place.adapter.out.persistence.FakePlaceRepository;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.domain.Place;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceQueryService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class PlaceQueryServiceWithFakeTest {

    private PlaceQueryService placeQueryService;

    private FakePlaceRepository fakePlaceRepository;
    private FakeDutyRepository fakeDutyRepository;
    private FakeMemberDutyRepository fakeMemberDutyRepository;
    private FakeMembersByUserIdQuery fakeMembersByUserIdQuery;
    private FakeCleaningInfoByMemberQuery fakeCleaningInfoByMemberQuery;
    private FakeCompletedChecklistQuery fakeCompletedChecklistQuery;
    private FakeUnreadNotificationCountQuery fakeUnreadNotificationCountQuery;
    private FakeCleaningsByPlaceQuery fakeCleaningsByPlaceQuery;
    private FakeChecklistForCalendarQuery fakeChecklistForCalendarQuery;
    private FakeMembersByCleaningQuery fakeMembersByCleaningQuery;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakePlaceRepository = new FakePlaceRepository();
        fakeDutyRepository = new FakeDutyRepository();
        fakeMemberDutyRepository = new FakeMemberDutyRepository();
        fakeMembersByUserIdQuery = new FakeMembersByUserIdQuery();
        fakeCleaningInfoByMemberQuery = new FakeCleaningInfoByMemberQuery();
        fakeCompletedChecklistQuery = new FakeCompletedChecklistQuery();
        fakeUnreadNotificationCountQuery = new FakeUnreadNotificationCountQuery();
        fakeCleaningsByPlaceQuery = new FakeCleaningsByPlaceQuery();
        fakeChecklistForCalendarQuery = new FakeChecklistForCalendarQuery();
        fakeMembersByCleaningQuery = new FakeMembersByCleaningQuery();

        // 서비스 생성
        placeQueryService = new PlaceQueryService(
                fakePlaceRepository,
                fakeDutyRepository,
                fakeMemberDutyRepository,
                fakeMembersByUserIdQuery,
                fakeCleaningInfoByMemberQuery,
                fakeCompletedChecklistQuery,
                fakeUnreadNotificationCountQuery,
                fakeCleaningsByPlaceQuery,
                fakeChecklistForCalendarQuery,
                fakeMembersByCleaningQuery
        );

        // MemberContext 설정
        mockPlace = PlaceJpaEntity.builder()
                .name("테스트 카페")
                .category(PlaceCategory.CAFE)
                .build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 1L);
        ReflectionTestUtils.setField(mockPlace, "startTime", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(mockPlace, "endTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(mockPlace, "isToday", true);

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
        fakePlaceRepository.clear();
        fakeDutyRepository.clear();
        fakeMemberDutyRepository.clear();
        fakeMembersByUserIdQuery.clear();
        fakeCleaningInfoByMemberQuery.clear();
        fakeCompletedChecklistQuery.clear();
        fakeUnreadNotificationCountQuery.clear();
        fakeCleaningsByPlaceQuery.clear();
        fakeChecklistForCalendarQuery.clear();
        fakeMembersByCleaningQuery.clear();
    }

    @Test
    void 플레이스_조회_대기중_멤버는_duty정보_없음() {
        // given
        Place place = createPlace(1L, "테스트 카페");
        fakePlaceRepository.save(place);

        mockContextMember = MemberJpaEntity.builder()
                .name("대기 멤버")
                .place(mockPlace)
                .role(MemberRole.WAITING)
                .status(false)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);
        MemberContext.set(mockContextMember);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.placeId()).isEqualTo(1L);
        assertThat(result.placeName()).isEqualTo("테스트 카페");
        assertThat(result.endTime()).isNull();
        assertThat(result.duty()).isNull();
    }

    @Test
    void 플레이스_조회_매니저_duty와_체크리스트_반환() {
        // given
        Place place = createPlace(1L, "테스트 카페");
        fakePlaceRepository.save(place);

        Duty duty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(duty);

        Member assignedMember = createMember(200L, "담당자", 1L);

        LocalDateTime now = LocalDateTime.now();
        ChecklistCalendarInfo checklist = new ChecklistCalendarInfo(
                10L, 100L, "바닥 청소", "청소 당번",
                false, null, null, false, now
        );
        fakeChecklistForCalendarQuery.addChecklist(checklist);
        fakeMembersByCleaningQuery.addMemberForCleaning(100L, assignedMember);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.placeId()).isEqualTo(1L);
        assertThat(result.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.duty()).isNotNull();
        assertThat(result.duty().dutyId()).isEqualTo(1L);
        assertThat(result.duty().dutyName()).isEqualTo("청소 당번");
        assertThat(result.duty().totalCleaning()).isEqualTo(1);
        assertThat(result.duty().endCleaning()).isEqualTo(0);
        assertThat(result.duty().checkLists()).hasSize(1);
        assertThat(result.duty().checkLists().get(0).cleaningName()).isEqualTo("바닥 청소");
        assertThat(result.duty().checkLists().get(0).members()).hasSize(1);
        assertThat(result.duty().checkLists().get(0).members().get(0).memberName()).isEqualTo("담당자");
    }

    @Test
    void 플레이스_조회_매니저_완료된_체크리스트_카운트() {
        // given
        Place place = createPlace(1L, "테스트 카페");
        fakePlaceRepository.save(place);

        Duty duty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(duty);

        LocalDateTime now = LocalDateTime.now();
        ChecklistCalendarInfo completedChecklist = new ChecklistCalendarInfo(
                10L, 100L, "바닥 청소", "청소 당번",
                true, 200L, now, false, now
        );
        ChecklistCalendarInfo incompleteChecklist = new ChecklistCalendarInfo(
                11L, 101L, "창문 닦기", "청소 당번",
                false, null, null, true, now
        );
        fakeChecklistForCalendarQuery.addChecklist(completedChecklist);
        fakeChecklistForCalendarQuery.addChecklist(incompleteChecklist);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.duty()).isNotNull();
        assertThat(result.duty().totalCleaning()).isEqualTo(2);
        assertThat(result.duty().endCleaning()).isEqualTo(1);
    }

    @Test
    void 플레이스_조회_일반멤버_본인_duty_체크리스트만_반환() {
        // given
        Place place = createPlace(1L, "테스트 카페");
        fakePlaceRepository.save(place);

        Duty duty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(duty);
        fakeMemberDutyRepository.addDuty(duty);
        fakeMemberDutyRepository.save(MemberDuty.of(100L, 1L));

        mockContextMember = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);
        MemberContext.set(mockContextMember);

        LocalDateTime now = LocalDateTime.now();
        ChecklistCalendarInfo checklist = new ChecklistCalendarInfo(
                10L, 100L, "바닥 청소", "청소 당번",
                false, null, null, false, now
        );
        fakeChecklistForCalendarQuery.addChecklist(checklist);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.duty()).isNotNull();
        assertThat(result.duty().dutyId()).isEqualTo(1L);
        assertThat(result.duty().dutyName()).isEqualTo("청소 당번");
    }

    @Test
    void 플레이스_조회_duty없으면_null반환() {
        // given
        Place place = createPlace(1L, "테스트 카페");
        fakePlaceRepository.save(place);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.duty()).isNull();
    }

    @Test
    void 플레이스_목록_조회_대기중_멤버_청소정보_없음() {
        // given
        Long userId = 1L;
        Place place = createPlace(10L, "테스트 카페");
        fakePlaceRepository.save(place);

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
        fakeMembersByUserIdQuery.addMember(waitingMember);

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
    void 플레이스_목록_조회_일반_멤버_청소진행률_포함() {
        // given
        Long userId = 1L;
        Place place = createPlace(10L, "테스트 카페");
        fakePlaceRepository.save(place);

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
        fakeMembersByUserIdQuery.addMember(member);
        fakeCleaningInfoByMemberQuery.setCleaningsForMember(100L, List.of(1L, 2L));
        fakeCompletedChecklistQuery.markAsCompleted(1L);
        fakeUnreadNotificationCountQuery.setUnreadCount(100L, 3);

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
    void 플레이스_목록_조회_매니저_전체_청소진행률() {
        // given
        Long userId = 1L;
        Place place = createPlace(10L, "테스트 카페");
        fakePlaceRepository.save(place);

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
        fakeMembersByUserIdQuery.addMember(manager);

        Cleaning cleaning1 = Cleaning.withId(new Cleaning.CleaningId(1L), "청소1", CleaningRepeatType.DAILY, null, null, false, 10L);
        Cleaning cleaning2 = Cleaning.withId(new Cleaning.CleaningId(2L), "청소2", CleaningRepeatType.DAILY, null, null, false, 10L);
        fakeCleaningsByPlaceQuery.addCleaning(cleaning1);
        fakeCleaningsByPlaceQuery.addCleaning(cleaning2);
        fakeCompletedChecklistQuery.markAsCompleted(1L);
        fakeCompletedChecklistQuery.markAsCompleted(2L);
        fakeUnreadNotificationCountQuery.setUnreadCount(100L, 0);

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
    void 플레이스_시간_조회() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                null,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );
        fakePlaceRepository.save(place);

        // when
        var result = placeQueryService.getTimeAndIsToday();

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
        fakePlaceRepository.save(place);

        // when
        String result = placeQueryService.getInviteCode();

        // then
        assertThat(result).isEqualTo("abc123");
    }

    @Test
    void 플레이스_종료시간_조회() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                null,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );
        fakePlaceRepository.save(place);

        // when
        LocalTime endTime = placeQueryService.getEndTimeByPlaceId(1L);

        // then
        assertThat(endTime).isEqualTo(LocalTime.of(18, 0));
    }

    private Place createPlace(Long placeId, String name) {
        return new Place(
                new Place.PlaceId(placeId),
                name,
                PlaceCategory.CAFE,
                "카페",
                null,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );
    }

    private Member createMember(Long memberId, String name, Long placeId) {
        return Member.withId(
                memberId,
                com.dangbun.domain.member.domain.MemberRole.MEMBER,
                name,
                true,
                Map.of(),
                placeId,
                1L,
                LocalDateTime.now()
        );
    }
}
