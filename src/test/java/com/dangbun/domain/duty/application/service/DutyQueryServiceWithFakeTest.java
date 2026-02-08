package com.dangbun.domain.duty.application.service;

import com.dangbun.domain.cleaning.application.port.in.query.FakeGetCleaningForDutyQuery;
import com.dangbun.domain.duty.adapter.out.persistence.FakeDutyRepository;
import com.dangbun.domain.duty.application.port.in.query.*;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.duty.exception.custom.DutyNotFoundException;
import com.dangbun.domain.membercleaning.application.port.in.query.FakeGetMemberCleaningForDutyQuery;
import com.dangbun.domain.memberduty.application.port.in.query.FakeGetMemberDutyForDutyQuery;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForDutyQuery.MemberDutyMemberInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DutyQueryService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class DutyQueryServiceWithFakeTest {

    private DutyQueryService dutyQueryService;

    private FakeDutyRepository fakeDutyRepository;
    private FakeGetMemberDutyForDutyQuery fakeGetMemberDutyForDutyQuery;
    private FakeGetCleaningForDutyQuery fakeGetCleaningForDutyQuery;
    private FakeGetMemberCleaningForDutyQuery fakeGetMemberCleaningForDutyQuery;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakeDutyRepository = new FakeDutyRepository();
        fakeGetMemberDutyForDutyQuery = new FakeGetMemberDutyForDutyQuery();
        fakeGetCleaningForDutyQuery = new FakeGetCleaningForDutyQuery();
        fakeGetMemberCleaningForDutyQuery = new FakeGetMemberCleaningForDutyQuery();

        // 서비스 생성
        dutyQueryService = new DutyQueryService(
                fakeDutyRepository,
                fakeGetMemberDutyForDutyQuery,
                fakeGetCleaningForDutyQuery,
                fakeGetMemberCleaningForDutyQuery
        );
    }

    @AfterEach
    void tearDown() {
        fakeDutyRepository.clear();
        fakeGetMemberDutyForDutyQuery.clear();
        fakeGetCleaningForDutyQuery.clear();
        fakeGetMemberCleaningForDutyQuery.clear();
    }

    // ==================== getDutyList 테스트 ====================

    @Test
    void 당번_목록_조회_성공() {
        // given
        Duty duty1 = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        Duty duty2 = Duty.withId(new Duty.DutyId(2L), "설거지 당번", DutyIcon.DISH_BLUE, 1L);
        fakeDutyRepository.save(duty1);
        fakeDutyRepository.save(duty2);

        // when
        DutyListResult result = dutyQueryService.getDutyList(1L);

        // then
        assertThat(result.duties()).hasSize(2);
        assertThat(result.duties())
                .extracting(DutyListResult.DutyItem::name)
                .containsExactlyInAnyOrder("청소 당번", "설거지 당번");
    }

    @Test
    void 당번_목록_조회_빈목록() {
        // given
        // 당번이 없는 상태

        // when
        DutyListResult result = dutyQueryService.getDutyList(1L);

        // then
        assertThat(result.duties()).isEmpty();
    }

    @Test
    void 당번_목록_조회_다른_플레이스_당번_제외() {
        // given
        Duty duty1 = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        Duty duty2 = Duty.withId(new Duty.DutyId(2L), "설거지 당번", DutyIcon.DISH_BLUE, 2L); // 다른 플레이스
        fakeDutyRepository.save(duty1);
        fakeDutyRepository.save(duty2);

        // when
        DutyListResult result = dutyQueryService.getDutyList(1L);

        // then
        assertThat(result.duties()).hasSize(1);
        assertThat(result.duties().get(0).name()).isEqualTo("청소 당번");
    }

    // ==================== getDutyMembers 테스트 ====================

    @Test
    void 당번_멤버_조회_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetMemberDutyForDutyQuery.addMemberInfo(1L, 10L, "MEMBER", "홍길동");
        fakeGetMemberDutyForDutyQuery.addMemberInfo(1L, 20L, "MANAGER", "김철수");

        // when
        DutyMembersResult result = dutyQueryService.getDutyMembers(1L);

        // then
        assertThat(result.members()).hasSize(2);

        // MANAGER가 먼저 나오고, 그 다음 MEMBER가 나와야 함
        assertThat(result.members().get(0).role()).isEqualTo("MANAGER");
        assertThat(result.members().get(0).name()).isEqualTo("김철수");
        assertThat(result.members().get(1).role()).isEqualTo("MEMBER");
        assertThat(result.members().get(1).name()).isEqualTo("홍길동");
    }

    @Test
    void 당번_멤버_조회_빈목록() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        // when
        DutyMembersResult result = dutyQueryService.getDutyMembers(1L);

        // then
        assertThat(result.members()).isEmpty();
    }

    @Test
    void 당번_멤버_조회_실패_당번이_존재하지_않음() {
        // given
        // 당번이 없는 상태

        // when, then
        assertThatThrownBy(() -> dutyQueryService.getDutyMembers(999L))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 당번_멤버_조회_이름순_정렬() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetMemberDutyForDutyQuery.addMemberInfo(1L, 10L, "MEMBER", "홍길동");
        fakeGetMemberDutyForDutyQuery.addMemberInfo(1L, 20L, "MEMBER", "김철수");
        fakeGetMemberDutyForDutyQuery.addMemberInfo(1L, 30L, "MEMBER", "이영희");

        // when
        DutyMembersResult result = dutyQueryService.getDutyMembers(1L);

        // then
        assertThat(result.members()).hasSize(3);
        assertThat(result.members())
                .extracting(DutyMembersResult.MemberItem::name)
                .containsExactly("김철수", "이영희", "홍길동");
    }

    // ==================== getDutyCleanings 테스트 ====================

    @Test
    void 당번_청소_조회_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetCleaningForDutyQuery.addCleaning(200L, "주방 청소", 1L);

        // when
        DutyCleaningsResult result = dutyQueryService.getDutyCleanings(1L);

        // then
        assertThat(result.cleanings()).hasSize(2);
        assertThat(result.cleanings())
                .extracting(DutyCleaningsResult.CleaningItem::name)
                .containsExactlyInAnyOrder("화장실 청소", "주방 청소");
    }

    @Test
    void 당번_청소_조회_빈목록() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        // when
        DutyCleaningsResult result = dutyQueryService.getDutyCleanings(1L);

        // then
        assertThat(result.cleanings()).isEmpty();
    }

    @Test
    void 당번_청소_조회_실패_당번이_존재하지_않음() {
        // given
        // 당번이 없는 상태

        // when, then
        assertThatThrownBy(() -> dutyQueryService.getDutyCleanings(999L))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== getCleaningInfoList 테스트 ====================

    @Test
    void 청소_정보_목록_조회_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetMemberCleaningForDutyQuery.addMemberNames(100L, List.of("홍길동", "김철수", "이영희"));

        // when
        CleaningInfoListResult result = dutyQueryService.getCleaningInfoList(1L);

        // then
        assertThat(result.cleanings()).hasSize(1);

        CleaningInfoListResult.CleaningInfo cleaningInfo = result.cleanings().get(0);
        assertThat(cleaningInfo.cleaningId()).isEqualTo(100L);
        assertThat(cleaningInfo.cleaningName()).isEqualTo("화장실 청소");
        assertThat(cleaningInfo.displayedNames()).containsExactly("홍길동", "김철수"); // 최대 2명
        assertThat(cleaningInfo.memberCount()).isEqualTo(3);
    }

    @Test
    void 청소_정보_목록_조회_멤버가_2명_이하일때() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetMemberCleaningForDutyQuery.addMemberNames(100L, List.of("홍길동"));

        // when
        CleaningInfoListResult result = dutyQueryService.getCleaningInfoList(1L);

        // then
        CleaningInfoListResult.CleaningInfo cleaningInfo = result.cleanings().get(0);
        assertThat(cleaningInfo.displayedNames()).containsExactly("홍길동");
        assertThat(cleaningInfo.memberCount()).isEqualTo(1);
    }

    @Test
    void 청소_정보_목록_조회_멤버가_없을때() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetMemberCleaningForDutyQuery.addMemberNames(100L, List.of());

        // when
        CleaningInfoListResult result = dutyQueryService.getCleaningInfoList(1L);

        // then
        CleaningInfoListResult.CleaningInfo cleaningInfo = result.cleanings().get(0);
        assertThat(cleaningInfo.displayedNames()).isEmpty();
        assertThat(cleaningInfo.memberCount()).isEqualTo(0);
    }

    @Test
    void 청소_정보_목록_조회_실패_당번이_존재하지_않음() {
        // given
        // 당번이 없는 상태

        // when, then
        assertThatThrownBy(() -> dutyQueryService.getCleaningInfoList(999L))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== findByIdAndPlaceId (GetDutyForMemberQuery) 테스트 ====================

    @Test
    void 당번_ID와_플레이스ID로_조회_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        // when
        Optional<GetDutyForMemberQuery.DutyInfo> result = dutyQueryService.findByIdAndPlaceId(1L, 1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().dutyId()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("청소 당번");
    }

    @Test
    void 당번_ID와_플레이스ID로_조회_빈결과_당번없음() {
        // given
        // 당번이 없는 상태

        // when
        Optional<GetDutyForMemberQuery.DutyInfo> result = dutyQueryService.findByIdAndPlaceId(1L, 1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 당번_ID와_플레이스ID로_조회_빈결과_다른_플레이스() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        // when
        Optional<GetDutyForMemberQuery.DutyInfo> result = dutyQueryService.findByIdAndPlaceId(1L, 999L);

        // then
        assertThat(result).isEmpty();
    }
}
