package com.dangbun.domain.cleaning.application.service;

import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.application.port.out.FakeCleaningRepository;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.application.port.in.query.FakeGetDutyForCleaningQuery;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.membercleaning.application.port.in.query.FakeGetMemberCleaningForCleaningQuery;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CleaningQueryService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class CleaningQueryServiceWithFakeTest {

    private CleaningQueryService cleaningQueryService;

    private FakeGetDutyForCleaningQuery fakeGetDutyForCleaningQuery;
    private FakeCleaningRepository fakeCleaningRepository;
    private FakeGetMemberCleaningForCleaningQuery fakeGetMemberCleaningForCleaningQuery;

    private PlaceJpaEntity testPlace;
    private MemberJpaEntity testMember;
    private DutyJpaEntity testDuty;

    @BeforeEach
    void setUp() throws Exception {
        // Fake 저장소 초기화
        fakeGetDutyForCleaningQuery = new FakeGetDutyForCleaningQuery();
        fakeCleaningRepository = new FakeCleaningRepository();
        fakeGetMemberCleaningForCleaningQuery = new FakeGetMemberCleaningForCleaningQuery();

        // 서비스 생성
        cleaningQueryService = new CleaningQueryService(
                fakeGetDutyForCleaningQuery,
                fakeCleaningRepository,
                fakeGetMemberCleaningForCleaningQuery
        );

        // 테스트용 Place 생성
        testPlace = PlaceJpaEntity.builder()
                .name("테스트 공간")
                .category(PlaceCategory.OFFICE)
                .categoryName("사무실")
                .build();
        setPlaceId(testPlace, 1L);

        // 테스트용 Member 생성
        testMember = MemberJpaEntity.builder()
                .name("테스트 멤버")
                .role(MemberRole.MANAGER)
                .status(true)
                .place(testPlace)
                .build();

        // 테스트용 Duty 생성
        testDuty = DutyJpaEntity.builder()
                .dutyId(1L)
                .name("청소 당번")
                .icon(DutyIcon.FLOOR_BLUE)
                .place(testPlace)
                .build();

        // Context 설정
        MemberContext.set(testMember);
        DutyContext.set(testDuty);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
        DutyContext.clear();
        fakeGetDutyForCleaningQuery.clear();
        fakeCleaningRepository.clear();
        fakeGetMemberCleaningForCleaningQuery.clear();
    }

    // PlaceJpaEntity의 placeId 설정을 위한 헬퍼 메서드
    private void setPlaceId(PlaceJpaEntity place, Long placeId) throws Exception {
        Field field = PlaceJpaEntity.class.getDeclaredField("placeId");
        field.setAccessible(true);
        field.set(place, placeId);
    }

    // ==================== getCleaningList 테스트 ====================

    @Test
    void 청소_목록_조회_성공_전체() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetDutyForCleaningQuery.addDuty(2L, "설거지 당번", "DISH_BLUE");

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(GetCleaningListResponse::name)
                .containsExactlyInAnyOrder("청소 당번", "설거지 당번");
    }

    @Test
    void 청소_목록_조회_성공_빈_목록() {
        // given
        // 당번이 없는 상태

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 청소_목록_조회_성공_멤버_필터링() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(List.of(10L, 20L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("청소 당번");
    }

    @Test
    void 청소_목록_조회시_빈_멤버_목록은_전체_조회() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetDutyForCleaningQuery.addDuty(2L, "설거지 당번", "DISH_BLUE");

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(List.of());

        // then
        assertThat(result).hasSize(2);
    }

    // ==================== getCleaningDetailList 테스트 ====================

    @Test
    void 청소_상세_목록_조회_성공() {
        // given
        Cleaning cleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning cleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning1);
        fakeCleaningRepository.save(cleaning2);

        fakeGetMemberCleaningForCleaningQuery.addMemberNames(1L, List.of("홍길동", "김철수", "이영희"));
        fakeGetMemberCleaningForCleaningQuery.addMemberNames(2L, List.of("박민수"));

        // when
        List<GetCleaningDetailListResponse> result = cleaningQueryService.getCleaningDetailList(null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 청소_상세_목록_조회시_멤버_2명까지만_표시() {
        // given
        Cleaning cleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning);

        fakeGetMemberCleaningForCleaningQuery.addMemberNames(1L, List.of("홍길동", "김철수", "이영희", "박민수"));

        // when
        List<GetCleaningDetailListResponse> result = cleaningQueryService.getCleaningDetailList(null);

        // then
        assertThat(result).hasSize(1);
        GetCleaningDetailListResponse response = result.get(0);
        assertThat(response.displayedMemberNames()).hasSize(2);
        assertThat(response.displayedMemberNames()).containsExactly("홍길동", "김철수");
        assertThat(response.memberCount()).isEqualTo(4);
    }

    @Test
    void 청소_상세_목록_조회시_멤버가_없는_경우() {
        // given
        Cleaning cleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning);

        fakeGetMemberCleaningForCleaningQuery.addMemberNames(1L, List.of());

        // when
        List<GetCleaningDetailListResponse> result = cleaningQueryService.getCleaningDetailList(null);

        // then
        assertThat(result).hasSize(1);
        GetCleaningDetailListResponse response = result.get(0);
        assertThat(response.displayedMemberNames()).isEmpty();
        assertThat(response.memberCount()).isEqualTo(0);
    }

    @Test
    void 청소_상세_목록_조회_빈_목록() {
        // given
        // 청소가 없는 상태

        // when
        List<GetCleaningDetailListResponse> result = cleaningQueryService.getCleaningDetailList(null);

        // then
        assertThat(result).isEmpty();
    }

    // ==================== getUnassignedCleanings 테스트 ====================

    @Test
    void 미지정_청소_조회_성공() {
        // given
        Cleaning assignedCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning unassignedCleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        Cleaning unassignedCleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(3L),
                "거실 청소",
                CleaningRepeatType.WEEKLY,
                "MONDAY,FRIDAY",
                null,
                false,
                1L
        );
        fakeCleaningRepository.save(assignedCleaning);
        fakeCleaningRepository.save(unassignedCleaning1);
        fakeCleaningRepository.save(unassignedCleaning2);

        // when
        List<GetCleaningUnassignedResponse> result = cleaningQueryService.getUnassignedCleanings();

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(GetCleaningUnassignedResponse::cleaningName)
                .containsExactlyInAnyOrder("주방 청소", "거실 청소");
    }

    @Test
    void 미지정_청소_조회_빈_목록() {
        // given
        Cleaning assignedCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(assignedCleaning);

        // when
        List<GetCleaningUnassignedResponse> result = cleaningQueryService.getUnassignedCleanings();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 미지정_청소_조회시_다른_플레이스_제외() {
        // given
        Cleaning unassignedCleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        Cleaning unassignedCleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "거실 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                2L
        );
        fakeCleaningRepository.save(unassignedCleaning1);
        fakeCleaningRepository.save(unassignedCleaning2);

        // when
        List<GetCleaningUnassignedResponse> result = cleaningQueryService.getUnassignedCleanings();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).cleaningName()).isEqualTo("주방 청소");
    }

    // ==================== getCleaning 테스트 ====================

    @Test
    void 청소_단건_조회_성공() {
        // given
        Cleaning cleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning);

        // when
        Cleaning result = cleaningQueryService.getCleaning(1L);

        // then
        assertThat(result.getCleaningId().value()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("화장실 청소");
        assertThat(result.getRepeatType()).isEqualTo(CleaningRepeatType.DAILY);
    }

    @Test
    void 청소_단건_조회_실패_청소가_존재하지_않음() {
        // given
        // 청소가 없는 상태

        // when, then
        assertThatThrownBy(() -> cleaningQueryService.getCleaning(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cleaning not found");
    }

    // ==================== getCleaningsByPlaceId 테스트 ====================

    @Test
    void 플레이스별_청소_조회_성공() {
        // given
        Cleaning cleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning cleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning cleaning3 = Cleaning.withId(
                new Cleaning.CleaningId(3L),
                "거실 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                2L
        );
        fakeCleaningRepository.save(cleaning1);
        fakeCleaningRepository.save(cleaning2);
        fakeCleaningRepository.save(cleaning3);

        // when
        List<Cleaning> result = cleaningQueryService.getCleaningsByPlaceId(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Cleaning::getName)
                .containsExactlyInAnyOrder("화장실 청소", "주방 청소");
    }

    @Test
    void 플레이스별_청소_조회_빈_목록() {
        // given
        // 청소가 없는 상태

        // when
        List<Cleaning> result = cleaningQueryService.getCleaningsByPlaceId(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 플레이스별_청소_조회시_미할당_청소_포함() {
        // given
        Cleaning assignedCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning unassignedCleaning = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        fakeCleaningRepository.save(assignedCleaning);
        fakeCleaningRepository.save(unassignedCleaning);

        // when
        List<Cleaning> result = cleaningQueryService.getCleaningsByPlaceId(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Cleaning::getName)
                .containsExactlyInAnyOrder("화장실 청소", "주방 청소");
    }
}
