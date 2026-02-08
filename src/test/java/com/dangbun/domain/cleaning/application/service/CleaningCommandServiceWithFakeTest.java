package com.dangbun.domain.cleaning.application.service;

import com.dangbun.domain.checklist.application.port.in.command.FakeCreateChecklistByDateAndTimeUseCase;
import com.dangbun.domain.checklist.application.port.in.query.FakeGetChecklistForCleaningQuery;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.application.port.out.FakeCleaningRepository;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaning.exception.custom.CleaningAlreadyExistsException;
import com.dangbun.domain.cleaning.exception.custom.CleaningNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.InvalidDateFormatException;
import com.dangbun.domain.cleaningImage.application.port.in.command.FakeCleaningImageCommandUseCase;
import com.dangbun.domain.cleaningdate.application.port.in.command.FakeCleaningDateForCleaningUseCase;
import com.dangbun.domain.duty.application.port.in.query.FakeGetDutyForCleaningQuery;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.FakeGetMemberForCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.in.command.FakeMemberCleaningForCleaningUseCase;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CleaningCommandService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class CleaningCommandServiceWithFakeTest {

    private CleaningCommandService cleaningCommandService;

    private FakeGetDutyForCleaningQuery fakeGetDutyForCleaningQuery;
    private FakeCleaningRepository fakeCleaningRepository;
    private FakeGetMemberForCleaningQuery fakeGetMemberForCleaningQuery;
    private FakeCleaningDateForCleaningUseCase fakeCleaningDateForCleaningUseCase;
    private FakeMemberCleaningForCleaningUseCase fakeMemberCleaningForCleaningUseCase;
    private FakeCreateChecklistByDateAndTimeUseCase fakeCreateChecklistByDateAndTimeUseCase;
    private FakeGetChecklistForCleaningQuery fakeGetChecklistForCleaningQuery;
    private FakeCleaningImageCommandUseCase fakeCleaningImageCommandUseCase;

    private PlaceJpaEntity testPlace;
    private MemberJpaEntity testMember;

    @BeforeEach
    void setUp() throws Exception {
        // Fake 저장소 초기화
        fakeGetDutyForCleaningQuery = new FakeGetDutyForCleaningQuery();
        fakeCleaningRepository = new FakeCleaningRepository();
        fakeGetMemberForCleaningQuery = new FakeGetMemberForCleaningQuery();
        fakeCleaningDateForCleaningUseCase = new FakeCleaningDateForCleaningUseCase();
        fakeMemberCleaningForCleaningUseCase = new FakeMemberCleaningForCleaningUseCase();
        fakeCreateChecklistByDateAndTimeUseCase = new FakeCreateChecklistByDateAndTimeUseCase();
        fakeGetChecklistForCleaningQuery = new FakeGetChecklistForCleaningQuery();
        fakeCleaningImageCommandUseCase = new FakeCleaningImageCommandUseCase();

        // 서비스 생성
        cleaningCommandService = new CleaningCommandService(
                fakeGetDutyForCleaningQuery,
                fakeCleaningRepository,
                fakeCleaningRepository,
                fakeGetMemberForCleaningQuery,
                fakeCleaningDateForCleaningUseCase,
                fakeMemberCleaningForCleaningUseCase,
                fakeCreateChecklistByDateAndTimeUseCase,
                fakeGetChecklistForCleaningQuery,
                fakeCleaningImageCommandUseCase
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

        // MemberContext 설정
        MemberContext.set(testMember);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
        fakeGetDutyForCleaningQuery.clear();
        fakeCleaningRepository.clear();
        fakeGetMemberForCleaningQuery.clear();
        fakeCleaningDateForCleaningUseCase.clear();
        fakeMemberCleaningForCleaningUseCase.clear();
        fakeCreateChecklistByDateAndTimeUseCase.clear();
        fakeGetChecklistForCleaningQuery.clear();
        fakeCleaningImageCommandUseCase.clear();
    }

    // PlaceJpaEntity의 placeId 설정을 위한 헬퍼 메서드
    private void setPlaceId(PlaceJpaEntity place, Long placeId) throws Exception {
        Field field = PlaceJpaEntity.class.getDeclaredField("placeId");
        field.setAccessible(true);
        field.set(place, placeId);
    }

    // ==================== createCleaning 테스트 ====================

    @Test
    void 청소_생성_성공_당번_지정() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetMemberForCleaningQuery.addMember(10L, "홍길동");
        fakeGetMemberForCleaningQuery.addMember(20L, "김철수");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of("홍길동", "김철수"),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01", "2025-08-02")
        );

        // when
        PostCleaningResponse response = cleaningCommandService.createCleaning(request);

        // then
        assertThat(response.cleaningId()).isNotNull();
        assertThat(fakeCleaningRepository.count()).isEqualTo(1);

        Cleaning savedCleaning = fakeCleaningRepository.findById(response.cleaningId()).get();
        assertThat(savedCleaning.getName()).isEqualTo("화장실 청소");
        assertThat(savedCleaning.getDutyId()).isEqualTo(1L);
        assertThat(savedCleaning.getNeedPhoto()).isTrue();
        assertThat(savedCleaning.getRepeatType()).isEqualTo(CleaningRepeatType.DAILY);
        assertThat(savedCleaning.getPlaceId()).isEqualTo(1L);
    }

    @Test
    void 청소_생성_성공_WEEKLY_반복() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "주간 청소",
                1L,
                List.of(),
                false,
                CleaningRepeatType.WEEKLY,
                List.of("MONDAY", "FRIDAY"),
                List.of("2025-08-01")
        );

        // when
        PostCleaningResponse response = cleaningCommandService.createCleaning(request);

        // then
        Cleaning savedCleaning = fakeCleaningRepository.findById(response.cleaningId()).get();
        assertThat(savedCleaning.getRepeatType()).isEqualTo(CleaningRepeatType.WEEKLY);
        assertThat(savedCleaning.getRepeatDays()).isEqualTo("MONDAY,FRIDAY");
    }

    @Test
    void 청소_생성_실패_당번이_존재하지_않음() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                999L,
                List.of(),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.createCleaning(request))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 청소_생성_실패_동일_당번에_같은_이름_청소_존재() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of(),
                false,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.createCleaning(request))
                .isInstanceOf(CleaningAlreadyExistsException.class);
    }

    @Test
    void 청소_생성_실패_잘못된_날짜_형식() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of(),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("invalid-date")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.createCleaning(request))
                .isInstanceOf(InvalidDateFormatException.class);
    }

    @Test
    void 청소_생성시_멤버_청소_관계_저장() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetMemberForCleaningQuery.addMember(10L, "홍길동");
        fakeGetMemberForCleaningQuery.addMember(20L, "김철수");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of("홍길동", "김철수"),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01")
        );

        // when
        PostCleaningResponse response = cleaningCommandService.createCleaning(request);

        // then
        List<Long> memberIds = fakeMemberCleaningForCleaningUseCase.getMemberIdsByCleaningId(response.cleaningId());
        assertThat(memberIds).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 청소_생성시_청소_날짜_저장() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of(),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01", "2025-08-02")
        );

        // when
        PostCleaningResponse response = cleaningCommandService.createCleaning(request);

        // then
        assertThat(fakeCleaningDateForCleaningUseCase.getDatesByCleaningId(response.cleaningId())).hasSize(2);
    }

    @Test
    void 청소_생성시_체크리스트_생성_호출() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "화장실 청소",
                1L,
                List.of(),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-01")
        );

        // when
        cleaningCommandService.createCleaning(request);

        // then
        assertThat(fakeCreateChecklistByDateAndTimeUseCase.getHistory()).hasSize(1);
    }

    // ==================== updateCleaning 테스트 ====================

    @Test
    void 청소_수정_성공() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetMemberForCleaningQuery.addMember(30L, "새멤버");

        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "변경된 청소",
                1L,
                List.of("새멤버"),
                false,
                CleaningRepeatType.WEEKLY,
                List.of(DayOfWeek.MONDAY),
                List.of("2025-08-10")
        );

        // when
        cleaningCommandService.updateCleaning(1L, request);

        // then
        Cleaning updatedCleaning = fakeCleaningRepository.findById(1L).get();
        assertThat(updatedCleaning.getName()).isEqualTo("변경된 청소");
        assertThat(updatedCleaning.getNeedPhoto()).isFalse();
        assertThat(updatedCleaning.getRepeatType()).isEqualTo(CleaningRepeatType.WEEKLY);
        assertThat(updatedCleaning.getRepeatDays()).isEqualTo("MONDAY");
    }

    @Test
    void 청소_수정_실패_청소가_존재하지_않음() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "변경된 청소",
                1L,
                List.of(),
                false,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-10")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.updateCleaning(999L, request))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    void 청소_수정_실패_당번이_존재하지_않음() {
        // given
        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "변경된 청소",
                999L,
                List.of(),
                false,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-10")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.updateCleaning(1L, request))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 청소_수정_실패_동일_당번에_같은_이름_청소_존재() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        Cleaning existingCleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        Cleaning existingCleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning1);
        fakeCleaningRepository.save(existingCleaning2);

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "주방 청소",
                1L,
                List.of(),
                false,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-10")
        );

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.updateCleaning(1L, request))
                .isInstanceOf(CleaningAlreadyExistsException.class);
    }

    @Test
    void 청소_수정시_기존_멤버_삭제후_새멤버_저장() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");
        fakeGetMemberForCleaningQuery.addMember(30L, "새멤버");

        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);
        fakeMemberCleaningForCleaningUseCase.saveAllByCleaningIdAndMemberIds(1L, List.of(10L, 20L));

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "화장실 청소",
                1L,
                List.of("새멤버"),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-10")
        );

        // when
        cleaningCommandService.updateCleaning(1L, request);

        // then
        List<Long> memberIds = fakeMemberCleaningForCleaningUseCase.getMemberIdsByCleaningId(1L);
        assertThat(memberIds).containsExactly(30L);
    }

    @Test
    void 청소_수정시_청소_날짜_삭제후_재저장() {
        // given
        fakeGetDutyForCleaningQuery.addDuty(1L, "청소 당번", "FLOOR_BLUE");

        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "화장실 청소",
                1L,
                List.of(),
                true,
                CleaningRepeatType.DAILY,
                null,
                List.of("2025-08-15", "2025-08-16", "2025-08-17")
        );

        // when
        cleaningCommandService.updateCleaning(1L, request);

        // then
        assertThat(fakeCleaningDateForCleaningUseCase.getDatesByCleaningId(1L)).hasSize(3);
    }

    // ==================== deleteCleaning 테스트 ====================

    @Test
    void 청소_삭제_성공() {
        // given
        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        // when
        cleaningCommandService.deleteCleaning(1L);

        // then
        assertThat(fakeCleaningRepository.findById(1L)).isEmpty();
        assertThat(fakeCleaningRepository.count()).isEqualTo(0);
    }

    @Test
    void 청소_삭제_실패_청소가_존재하지_않음() {
        // given
        // 청소가 없는 상태

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.deleteCleaning(999L))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    void 청소_삭제시_체크리스트_이미지_삭제() {
        // given
        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                1L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);
        fakeGetChecklistForCleaningQuery.addChecklistIds(1L, List.of(100L, 200L));

        // when
        cleaningCommandService.deleteCleaning(1L);

        // then
        assertThat(fakeCleaningImageCommandUseCase.getDeleteS3History()).containsExactlyInAnyOrder(100L, 200L);
    }

    // ==================== assignCleaningsToDuty 테스트 ====================

    @Test
    void 당번에_청소_할당_성공() {
        // given
        Cleaning cleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        Cleaning cleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning1);
        fakeCleaningRepository.save(cleaning2);

        // when
        cleaningCommandService.assignCleaningsToDuty(10L, List.of(1L, 2L));

        // then
        assertThat(fakeCleaningRepository.findById(1L).get().getDutyId()).isEqualTo(10L);
        assertThat(fakeCleaningRepository.findById(2L).get().getDutyId()).isEqualTo(10L);
    }

    @Test
    void 당번에_청소_할당시_이미_할당된_청소는_변경하지_않음() {
        // given
        Cleaning cleaning1 = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                5L,
                true,
                1L
        );
        Cleaning cleaning2 = Cleaning.withId(
                new Cleaning.CleaningId(2L),
                "주방 청소",
                CleaningRepeatType.DAILY,
                null,
                null,
                true,
                1L
        );
        fakeCleaningRepository.save(cleaning1);
        fakeCleaningRepository.save(cleaning2);

        // when
        cleaningCommandService.assignCleaningsToDuty(10L, List.of(1L, 2L));

        // then
        assertThat(fakeCleaningRepository.findById(1L).get().getDutyId()).isEqualTo(5L);
        assertThat(fakeCleaningRepository.findById(2L).get().getDutyId()).isEqualTo(10L);
    }

    // ==================== removeCleaningFromDuty 테스트 ====================

    @Test
    void 당번에서_청소_제거_성공() {
        // given
        Cleaning existingCleaning = Cleaning.withId(
                new Cleaning.CleaningId(1L),
                "화장실 청소",
                CleaningRepeatType.DAILY,
                null,
                10L,
                true,
                1L
        );
        fakeCleaningRepository.save(existingCleaning);

        // when
        cleaningCommandService.removeCleaningFromDuty(1L);

        // then
        assertThat(fakeCleaningRepository.findById(1L).get().getDutyId()).isNull();
    }

    @Test
    void 당번에서_청소_제거_실패_청소가_존재하지_않음() {
        // given
        // 청소가 없는 상태

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.removeCleaningFromDuty(999L))
                .isInstanceOf(CleaningNotFoundException.class);
    }
}
