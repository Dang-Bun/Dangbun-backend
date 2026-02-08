package com.dangbun.domain.duty.application.service;

import com.dangbun.domain.cleaning.application.port.in.command.FakeCleaningForDutyUseCase;
import com.dangbun.domain.cleaning.application.port.in.query.FakeGetCleaningForDutyQuery;
import com.dangbun.domain.duty.adapter.out.persistence.FakeDutyRepository;
import com.dangbun.domain.duty.application.port.in.command.*;
import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import com.dangbun.domain.duty.application.port.in.query.AddMembersResult;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyAssignType;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.member.application.port.in.query.FakeGetMembersForDutyQuery;
import com.dangbun.domain.membercleaning.application.port.in.command.FakeMemberCleaningForDutyUseCase;
import com.dangbun.domain.memberduty.application.port.in.command.FakeMemberDutyForDutyUseCase;
import com.dangbun.domain.memberduty.application.port.in.query.FakeGetMemberDutyForDutyQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DutyCommandService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class DutyCommandServiceWithFakeTest {

    private DutyCommandService dutyCommandService;

    private FakeDutyRepository fakeDutyRepository;
    private FakeGetMembersForDutyQuery fakeGetMembersForDutyQuery;
    private FakeMemberDutyForDutyUseCase fakeMemberDutyForDutyUseCase;
    private FakeGetMemberDutyForDutyQuery fakeGetMemberDutyForDutyQuery;
    private FakeGetCleaningForDutyQuery fakeGetCleaningForDutyQuery;
    private FakeCleaningForDutyUseCase fakeCleaningForDutyUseCase;
    private FakeMemberCleaningForDutyUseCase fakeMemberCleaningForDutyUseCase;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakeDutyRepository = new FakeDutyRepository();
        fakeGetMembersForDutyQuery = new FakeGetMembersForDutyQuery();
        fakeMemberDutyForDutyUseCase = new FakeMemberDutyForDutyUseCase();
        fakeGetMemberDutyForDutyQuery = new FakeGetMemberDutyForDutyQuery();
        fakeGetCleaningForDutyQuery = new FakeGetCleaningForDutyQuery();
        fakeCleaningForDutyUseCase = new FakeCleaningForDutyUseCase();
        fakeMemberCleaningForDutyUseCase = new FakeMemberCleaningForDutyUseCase();

        // 서비스 생성
        dutyCommandService = new DutyCommandService(
                fakeDutyRepository,
                fakeDutyRepository,
                fakeGetMembersForDutyQuery,
                fakeMemberDutyForDutyUseCase,
                fakeGetMemberDutyForDutyQuery,
                fakeGetCleaningForDutyQuery,
                fakeCleaningForDutyUseCase,
                fakeMemberCleaningForDutyUseCase
        );
    }

    @AfterEach
    void tearDown() {
        fakeDutyRepository.clear();
        fakeGetMembersForDutyQuery.clear();
        fakeMemberDutyForDutyUseCase.clear();
        fakeGetMemberDutyForDutyQuery.clear();
        fakeGetCleaningForDutyQuery.clear();
        fakeCleaningForDutyUseCase.clear();
        fakeMemberCleaningForDutyUseCase.clear();
    }

    // ==================== createDuty 테스트 ====================

    @Test
    void 당번_생성_성공() {
        // given
        CreateDutyCommand command = new CreateDutyCommand(1L, "청소 당번", DutyIcon.FLOOR_BLUE);

        // when
        Long dutyId = dutyCommandService.createDuty(command);

        // then
        assertThat(dutyId).isNotNull();
        assertThat(fakeDutyRepository.findById(dutyId)).isPresent();

        Duty savedDuty = fakeDutyRepository.findById(dutyId).get();
        assertThat(savedDuty.getName()).isEqualTo("청소 당번");
        assertThat(savedDuty.getIcon()).isEqualTo(DutyIcon.FLOOR_BLUE);
        assertThat(savedDuty.getPlaceId()).isEqualTo(1L);
    }

    @Test
    void 당번_생성_실패_동일_플레이스에_같은_이름_당번_존재() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        CreateDutyCommand command = new CreateDutyCommand(1L, "청소 당번", DutyIcon.BUCKET_PINK);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.createDuty(command))
                .isInstanceOf(DutyAlreadyExistsException.class);
    }

    @Test
    void 당번_생성_성공_다른_플레이스에_같은_이름() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        CreateDutyCommand command = new CreateDutyCommand(2L, "청소 당번", DutyIcon.BUCKET_PINK);

        // when
        Long dutyId = dutyCommandService.createDuty(command);

        // then
        assertThat(dutyId).isNotNull();
        assertThat(fakeDutyRepository.count()).isEqualTo(2);
    }

    // ==================== updateDuty 테스트 ====================

    @Test
    void 당번_수정_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        UpdateDutyCommand command = new UpdateDutyCommand(1L, "변경된 당번", DutyIcon.CLEANER_PINK);

        // when
        UpdateDutyResult result = dutyCommandService.updateDuty(command);

        // then
        assertThat(result.dutyId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("변경된 당번");
        assertThat(result.icon()).isEqualTo(DutyIcon.CLEANER_PINK);

        Duty updatedDuty = fakeDutyRepository.findById(1L).get();
        assertThat(updatedDuty.getName()).isEqualTo("변경된 당번");
        assertThat(updatedDuty.getIcon()).isEqualTo(DutyIcon.CLEANER_PINK);
    }

    @Test
    void 당번_수정_실패_당번이_존재하지_않음() {
        // given
        UpdateDutyCommand command = new UpdateDutyCommand(999L, "변경된 당번", DutyIcon.CLEANER_PINK);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.updateDuty(command))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== deleteDuty 테스트 ====================

    @Test
    void 당번_삭제_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        // when
        dutyCommandService.deleteDuty(1L);

        // then
        assertThat(fakeDutyRepository.findById(1L)).isEmpty();
        assertThat(fakeDutyRepository.count()).isEqualTo(0);
    }

    @Test
    void 당번_삭제_실패_당번이_존재하지_않음() {
        // given
        // 당번이 없는 상태

        // when, then
        assertThatThrownBy(() -> dutyCommandService.deleteDuty(999L))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== addMembers 테스트 ====================

    @Test
    void 당번에_멤버_추가_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetMembersForDutyQuery.addMember(10L, "홍길동");
        fakeGetMembersForDutyQuery.addMember(20L, "김철수");

        AddMembersCommand command = new AddMembersCommand(1L, List.of(10L, 20L));

        // when
        AddMembersResult result = dutyCommandService.addMembers(command);

        // then
        assertThat(result.addedMemberIds()).containsExactlyInAnyOrder(10L, 20L);
        assertThat(fakeMemberDutyForDutyUseCase.getMemberIdsByDutyId(1L)).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 당번에_멤버_추가_실패_당번이_존재하지_않음() {
        // given
        fakeGetMembersForDutyQuery.addMember(10L, "홍길동");

        AddMembersCommand command = new AddMembersCommand(999L, List.of(10L));

        // when, then
        assertThatThrownBy(() -> dutyCommandService.addMembers(command))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 당번에_멤버_추가_실패_일부_멤버가_존재하지_않음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetMembersForDutyQuery.addMember(10L, "홍길동");
        // 20L 멤버는 등록하지 않음

        AddMembersCommand command = new AddMembersCommand(1L, List.of(10L, 20L));

        // when, then
        assertThatThrownBy(() -> dutyCommandService.addMembers(command))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 당번에_멤버_추가시_기존_멤버_삭제후_새로_추가() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeMemberDutyForDutyUseCase.saveAllByDutyId(1L, List.of(10L, 20L));

        fakeGetMembersForDutyQuery.addMember(30L, "새멤버");

        AddMembersCommand command = new AddMembersCommand(1L, List.of(30L));

        // when
        AddMembersResult result = dutyCommandService.addMembers(command);

        // then
        assertThat(result.addedMemberIds()).containsExactly(30L);
        assertThat(fakeMemberDutyForDutyUseCase.getMemberIdsByDutyId(1L)).containsExactly(30L);
    }

    // ==================== assignMember - CUSTOM 테스트 ====================

    @Test
    void 멤버_할당_CUSTOM_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);

        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.CUSTOM, 100L, List.of(10L, 20L), null
        );

        // when
        dutyCommandService.assignMember(command);

        // then
        assertThat(fakeMemberCleaningForDutyUseCase.getMemberIdsByCleaningId(100L))
                .containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 멤버_할당_CUSTOM_멤버아이디_null이면_기존_멤버만_삭제() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeMemberCleaningForDutyUseCase.saveAllByCleaningIdAndMemberIds(100L, List.of(10L, 20L));

        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.CUSTOM, 100L, null, null
        );

        // when
        dutyCommandService.assignMember(command);

        // then
        assertThat(fakeMemberCleaningForDutyUseCase.getMemberIdsByCleaningId(100L)).isEmpty();
    }

    @Test
    void 멤버_할당_CUSTOM_실패_청소가_해당_당번에_존재하지_않음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 2L); // 다른 당번에 속함

        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.CUSTOM, 100L, List.of(10L), null
        );

        // when, then
        assertThatThrownBy(() -> dutyCommandService.assignMember(command))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    // ==================== assignMember - COMMON 테스트 ====================

    @Test
    void 멤버_할당_COMMON_성공_모든_청소에_모든_멤버_할당() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetCleaningForDutyQuery.addCleaning(200L, "주방 청소", 1L);

        fakeGetMemberDutyForDutyQuery.addMemberIds(1L, List.of(10L, 20L));
        fakeGetMembersForDutyQuery.addMember(10L, "홍길동");
        fakeGetMembersForDutyQuery.addMember(20L, "김철수");

        // COMMON 타입에서도 cleaningId가 필요하지만, 실제 로직에서는 모든 청소를 순회함
        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.COMMON, 100L, null, null
        );

        // when
        dutyCommandService.assignMember(command);

        // then
        assertThat(fakeMemberCleaningForDutyUseCase.getMemberIdsByCleaningId(100L))
                .containsExactlyInAnyOrder(10L, 20L);
        assertThat(fakeMemberCleaningForDutyUseCase.getMemberIdsByCleaningId(200L))
                .containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 멤버_할당_COMMON_실패_당번에_멤버가_없음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetMemberDutyForDutyQuery.addMemberIds(1L, List.of());

        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.COMMON, 100L, null, null
        );

        // when, then
        assertThatThrownBy(() -> dutyCommandService.assignMember(command))
                .isInstanceOf(MemberNotExistsException.class);
    }

    // ==================== assignMember - RANDOM 테스트 ====================

    @Test
    void 멤버_할당_RANDOM_성공_지정된_수만큼_랜덤_할당() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);
        fakeGetCleaningForDutyQuery.addCleaning(200L, "주방 청소", 1L);

        fakeGetMemberDutyForDutyQuery.addMemberIds(1L, List.of(10L, 20L, 30L));
        fakeGetMembersForDutyQuery.addMember(10L, "홍길동");
        fakeGetMembersForDutyQuery.addMember(20L, "김철수");
        fakeGetMembersForDutyQuery.addMember(30L, "이영희");

        AssignMemberCommand command = new AssignMemberCommand(
                1L, DutyAssignType.RANDOM, null, null, 2
        );

        // when
        dutyCommandService.assignMember(command);

        // then
        assertThat(fakeMemberCleaningForDutyUseCase.getAssignedMemberCount(100L)).isEqualTo(2);
        assertThat(fakeMemberCleaningForDutyUseCase.getAssignedMemberCount(200L)).isEqualTo(2);
    }

    @Test
    void 멤버_할당_실패_당번이_존재하지_않음() {
        // given
        AssignMemberCommand command = new AssignMemberCommand(
                999L, DutyAssignType.COMMON, 100L, null, null
        );

        // when, then
        assertThatThrownBy(() -> dutyCommandService.assignMember(command))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== addCleanings 테스트 ====================

    @Test
    void 당번에_청소_추가_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", null);
        fakeGetCleaningForDutyQuery.addCleaning(200L, "주방 청소", null);

        AddCleaningsCommand command = new AddCleaningsCommand(1L, List.of(100L, 200L));

        // when
        AddCleaningsResult result = dutyCommandService.addCleanings(command);

        // then
        assertThat(result.assignedCleaningIds()).containsExactlyInAnyOrder(100L, 200L);
        assertThat(fakeCleaningForDutyUseCase.getAssignmentHistory()).hasSize(1);
        assertThat(fakeCleaningForDutyUseCase.getAssignmentHistory().get(0).dutyId()).isEqualTo(1L);
    }

    @Test
    void 당번에_청소_추가_이미_할당된_청소는_제외() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", null);
        fakeGetCleaningForDutyQuery.addCleaning(200L, "주방 청소", 2L); // 이미 다른 당번에 할당됨

        AddCleaningsCommand command = new AddCleaningsCommand(1L, List.of(100L, 200L));

        // when
        AddCleaningsResult result = dutyCommandService.addCleanings(command);

        // then
        assertThat(result.assignedCleaningIds()).containsExactly(100L);
    }

    @Test
    void 당번에_청소_추가_실패_당번이_존재하지_않음() {
        // given
        AddCleaningsCommand command = new AddCleaningsCommand(999L, List.of(100L));

        // when, then
        assertThatThrownBy(() -> dutyCommandService.addCleanings(command))
                .isInstanceOf(DutyNotFoundException.class);
    }

    // ==================== removeCleaning 테스트 ====================

    @Test
    void 당번에서_청소_제거_성공() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 1L);

        RemoveCleaningCommand command = new RemoveCleaningCommand(1L, 100L);

        // when
        dutyCommandService.removeCleaning(command);

        // then
        assertThat(fakeCleaningForDutyUseCase.getRemoveHistory()).contains(100L);
    }

    @Test
    void 당번에서_청소_제거_실패_청소가_존재하지_않음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        RemoveCleaningCommand command = new RemoveCleaningCommand(1L, 999L);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.removeCleaning(command))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    void 당번에서_청소_제거_실패_청소가_해당_당번에_할당되지_않음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", 2L); // 다른 당번에 할당됨

        RemoveCleaningCommand command = new RemoveCleaningCommand(1L, 100L);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.removeCleaning(command))
                .isInstanceOf(CleaningNotAssignedException.class);
    }

    @Test
    void 당번에서_청소_제거_실패_청소가_어떤_당번에도_할당되지_않음() {
        // given
        Duty existingDuty = Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.FLOOR_BLUE, 1L);
        fakeDutyRepository.save(existingDuty);

        fakeGetCleaningForDutyQuery.addCleaning(100L, "화장실 청소", null); // 할당되지 않음

        RemoveCleaningCommand command = new RemoveCleaningCommand(1L, 100L);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.removeCleaning(command))
                .isInstanceOf(CleaningNotAssignedException.class);
    }
}
